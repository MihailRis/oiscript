package mihailris.oiscript.jit;

import mihailris.oiscript.OiNone;
import mihailris.oiscript.Operators;
import mihailris.oiscript.RawFunction;
import mihailris.oiscript.parsing.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class JitFunctionCompiler {
    private static final String CLS_MATH = "java/lang/Math";
    private static final String CLS_ARITHMETICS = "mihailris/oiscript/Arithmetics";
    private static final String CLS_LOGICS = "mihailris/oiscript/Logics";
    private static final String CLS_OIUTILS = "mihailris/oiscript/OiUtils";
    private static final String CLS_SCRIPT = "mihailris/oiscript/Script";
    private static final String DESC_ARITHMETICS = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String DESC_LOGICS = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
    private static final String DESC_EXECUTE = "(Lmihailris/oiscript/runtime/Function;Lmihailris/oiscript/runtime/OiRunHandle;[Ljava/lang/Object;)Ljava/lang/Object;";

    private final RawFunction function;
    private final MethodVisitor methodVisitor;
    private static final JitLogger logger = new JitLogger();

    public JitFunctionCompiler(RawFunction function, MethodVisitor methodVisitor) {
        this.function = function;
        this.methodVisitor = methodVisitor;
    }

    public void compileFunction(CompilerContext context, String methodName, String descriptor) {
        logger.comment("========= "+methodName+":"+descriptor+" =========");
        List<Command> commands = function.getCommands();
        for (Command command : commands) {
            compile(command, context);
        }
        returnNone();
        logger.comment("===============================");
    }

    public void compileAdapter(String className, String name, String descriptor) {
        logger.comment("========= execute (ADAPTER) =========");
        List<String> args = function.getArgs();

        loadThis();
        pushContext();

        for (int i = 0; i < args.size(); i++) {
            aload(0);
            iconst(i);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            logger.log("aaload");
        }
        invokeVirtual(className, name, descriptor);
        methodVisitor.visitInsn(Opcodes.ARETURN);
        logger.log("areturn");
        logger.comment("===============================");
    }

    private void compile(Command command, CompilerContext context) {
        if (command instanceof Return) {
            Return ret = (Return) command;
            Value value = ret.getValue();
            if (value == null) {
                returnNone();
                return;
            } else {
                compile(ret.getValue(), context);
            }
            methodVisitor.visitInsn(Opcodes.ARETURN);
            logger.log("areturn");
        } else if (command instanceof LocalAssignment) {
            LocalAssignment assignment = (LocalAssignment) command;
            Value value = assignment.getValue();
            int index = assignment.getIndex();

            String operator = assignment.getOperator();
            if (operator.equals("=")) {
                astore(context, index, value);
            } else {
                astore(index, () -> {
                    aload(index);
                    OiType tright = compileTyped(value, context);
                    boxValue(tright);
                    OiType type = binaryOperation(operator.substring(0, operator.length()-1), OiType.OBJECT, tright);
                    boxValue(type);
                });
            }
        } else if (command instanceof While) {
            While loop = (While) command;
            Value condition = loop.getCondition();
            Label start = new Label();
            Label end = new Label();
            label(start);

            if (condition instanceof Not) {
                compileCondition(((Not) condition).getValue(), context);
                ifne(end);
            } else {
                compileCondition(condition, context);
                ifeq(end);
            }

            CompilerContext loopContext = context.loop(start, end);
            for (Command subcommand : loop.getCommands()) {
                compile(subcommand, loopContext);
            }
            jmp(start);
            label(end);
        } else if (command instanceof ForLoop) {
            ForLoop forLoop = (ForLoop) command;
            int index = forLoop.getIndex();
            Value value = forLoop.getIterable();
            int iteratorIndex = context.newLocal();
            compile(value, context);
            astore(iteratorIndex, () ->
                    invokeStatic(CLS_OIUTILS, "iterator", "(Ljava/lang/Object;)Ljava/util/Iterator;"));
            Label start = new Label();
            Label end = new Label();
            label(start);
            aload(iteratorIndex);
            invokeInterface("java/util/Iterator", "hasNext", "()Z");
            ifeq(end);

            aload(iteratorIndex);

            astore(index, () ->
                    invokeInterface("java/util/Iterator", "next", "()Ljava/lang/Object;"));

            CompilerContext loopContext = context.loop(start, end);
            for (Command subcommand : forLoop.getCommands()) {
                compile(subcommand, loopContext);
            }
            jmp(start);

            label(end);
        } else if (command instanceof If) {
            If branch = (If) command;
            Value condition = branch.getCondition();
            List<Elif> elifs = branch.getElifs();
            Else elseblock = branch.getElseBlock();
            Label end = new Label();
            Label next = new Label();

            if (condition instanceof Not) {
                compileCondition(((Not) condition).getValue(), context);
                ifne(next);
            } else {
                compileCondition(condition, context);
                ifeq(next);
            }
            for (Command subcommand : branch.getCommands()) {
                compile(subcommand, context);
            }
            if (elseblock != null || !elifs.isEmpty()) {
                jmp(end);
            }
            label(next);

            for (int i = 0; i < elifs.size(); i++) {
                Elif elif = elifs.get(i);
                next = new Label();
                Value elifCondition = elif.getCondition();
                if (elifCondition instanceof Not) {
                    compileCondition(((Not) elifCondition).getValue(), context);
                    ifne(next);
                } else {
                    compileCondition(elifCondition, context);
                    ifeq(next);
                }
                for (Command subcommand : elif.getCommands()) {
                    compile(subcommand, context);
                }
                if (elseblock != null || i+1 < elifs.size()) {
                    jmp(end);
                }
                label(next);
            }
            if (elseblock != null) {
                for (Command subcommand : elseblock.getCommands()) {
                    compile(subcommand, context);
                }
            }
            label(end);
        } else if (command instanceof ItemAssignment) {
            ItemAssignment assignment = (ItemAssignment) command;
            Value source = assignment.getSource();
            Value key = assignment.getKey();
            Value value = assignment.getValue();
            compile(source, context);
            compileIndex(key, context);
            compile(value, context);
            invokeInterface("java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;");
            pop();
        } else if (command instanceof ValueWrapper) {
            ValueWrapper wrapper = (ValueWrapper) command;
            compile(wrapper.getValue(), context);
            pop();
        } else if (command instanceof Print) {
            Print print = (Print) command;
            Value value = print.getValue();
            getstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
            compile(value, context);
            invokeVirtual("java/io/PrintStream", "print", "(Ljava/lang/Object;)V");
        } else if (command instanceof Break) {
            logger.comment("break");
            jmp(context.getLoopEnd());
        } else if (command instanceof Continue) {
            logger.comment("continue");
            jmp(context.getLoopStart());
        }
        else if (!(command instanceof Pass)){
            throw new IllegalStateException(command.getClass().getSimpleName()+" is not supported yet");
        }
    }

    private void compileIndex(Value value, CompilerContext context) {
        compile(value, context);

        checkcast("java/lang/Number");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
    }

    private void jmp(Label label) {
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
        logger.log("goto ", label);
    }

    private void ifeq(Label label) {
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label);
        logger.log("ifeq ", label);
    }

    private void ifne(Label label) {
        methodVisitor.visitJumpInsn(Opcodes.IFNE, label);
        logger.log("ifne ", label);
    }

    private void compileCondition(Value condition, CompilerContext context) {
        OiType type = compileTyped(condition, context);
        if (type != OiType.BOOL) {
            boxValue(type);
            invokeStatic("mihailris/oiscript/Logics", "isTrue", "(Ljava/lang/Object;)Z");
        }
    }

    private void compileLong(Value condition, CompilerContext context) {
        OiType type = compileTyped(condition, context);
        if (type != OiType.LONG) {
            boxValue(type);
            checkcast("java/lang/Number");
            invokeVirtual("java/lang/Number", "longValue", "()J");
        }
    }

    private void label(Label label) {
        methodVisitor.visitLabel(label);
        logger.log(label);
    }

    private void invokeInterface(String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true);
        logger.log("invokeinterface ", owner+"."+name+":"+descriptor);
    }

    private void invokeVirtual(String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false);
        logger.log("invokevirtual ", owner+"."+name+":"+descriptor);
    }

    private void invokeStatic(String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, descriptor, false);
        logger.log("invokestatic ", owner+"."+name+":"+descriptor);
    }

    /**
     * @param index index of local variable. Actual JVM index is index+2
     */
    private void aload(int index) {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, index+2);
        logger.log("aload ", index+2);
    }

    private void astore(int index, Runnable value) {
        value.run();
        methodVisitor.visitVarInsn(Opcodes.ASTORE, index + 2);
        logger.log("astore ", index+2);
    }

    private void astore(CompilerContext context, int index, Value value) {
        compile(value, context);
        methodVisitor.visitVarInsn(Opcodes.ASTORE, index + 2);
        logger.log("astore ",index + 2);
    }

    private void compile(Value value, CompilerContext context) {
        OiType type = compileTyped(value, context);
        if (type != OiType.OBJECT)
            boxValue(type);
    }

    private void boxValue(OiType type) {
        switch (type) {
            case BOOL:
                invokeStatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                break;
            case LONG:
                invokeStatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                break;
            case DOUBLE:
                invokeStatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                break;
        }
    }

    private OiType binaryOperation(BinaryOperator operator, CompilerContext context) {
        Value left = operator.getLeft();
        Value right = operator.getRight();
        OiType tleft = left.getType();
        OiType tright = right.getType();

        compileTyped(left, context);
        if (tleft == OiType.OBJECT || tright == OiType.OBJECT)
            boxValue(tleft);
        if (tleft == OiType.DOUBLE || tright == OiType.DOUBLE)
            cast(tright, tleft);

        compileTyped(right, context);
        if (tleft == OiType.OBJECT || tright == OiType.OBJECT)
            boxValue(tright);
        if (tleft == OiType.DOUBLE)
            cast(tright, tleft);
        return binaryOperation(operator.getOperator(), tleft, tright);
    }

    private OiType compileTyped(Value value, CompilerContext context) {
        value = value.optimize();
        if (value instanceof BinaryOperator) {
            BinaryOperator operator = (BinaryOperator) value;
            return binaryOperation(operator, context);
        }
        else if (value instanceof LocalName) {
            LocalName variable = (LocalName) value;
            int index = variable.getIndex();
            aload(index);
        }
        else if (value instanceof IntegerValue) {
            IntegerValue integerValue = (IntegerValue) value;
            lconst(integerValue.getValue());
            return OiType.LONG;
        }
        else if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            iconst(booleanValue.getValue() ? 1 : 0);
            return OiType.BOOL;
        }
        else if (value instanceof NumberValue) {
            NumberValue numberValue = (NumberValue) value;
            dconst(numberValue.getValue());
            return OiType.DOUBLE;
        }
        else if (value instanceof OiNone) {
            pushNone();
        }
        else if (value instanceof ListValue) {
            logger.comment("list literal");
            ListValue listValue = (ListValue) value;
            List<Value> values = listValue.getValues();
            iconst(values.size());
            anewarray();
            for (int i = 0; i < values.size(); i++) {
                methodVisitor.visitInsn(Opcodes.DUP);
                logger.log("dup");
                iconst(i);
                compile(values.get(i), context);
                methodVisitor.visitInsn(Opcodes.AASTORE);
                logger.log("aastore");
            }
            invokeStatic("java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
        }
        else if (value instanceof StringValue) {
            StringValue stringValue = (StringValue) value;
            ldc(stringValue.getValue());
        }
        else if (value instanceof Negative) {
            Negative negative = (Negative) value;
            OiType type = compileTyped(negative.getValue(), context);
            return negate(type);
        }
        else if (value instanceof ItemValue) {
            ItemValue itemValue = (ItemValue) value;
            compile(itemValue.getSource(), context);
            compileIndex(itemValue.getKey(), context);
            invokeInterface("java/util/List", "get", "(I)Ljava/lang/Object;");
        }
        else if (value instanceof AttributeValue) {
            AttributeValue attributeValue = (AttributeValue) value;
            Value source = attributeValue.getSource();
            String name = attributeValue.getName();

            compile(source, context);
            if (name.equals("len")) {
                invokeStatic(CLS_OIUTILS, "length", "(Ljava/lang/Object;)J");
                return OiType.LONG;
            }
            throw new IllegalStateException("not implemented for "+source+"->"+name);
        }
        else if (value instanceof NamedValue) {
            NamedValue namedValue = (NamedValue) value;
            pushContext();
            String name = namedValue.getName();
            ldc(name);
            invokeVirtual("mihailris/oiscript/Context", "get", "(Ljava/lang/String;)Ljava/lang/Object;");
        }
        else if (value instanceof Call) {
            Call call = (Call) value;
            Value source = call.getSource();
            List<Value> values = call.getValues();

            logger.comment("calling function "+source+" BEGIN");

            pushContext();
            getfield("mihailris/oiscript/Context", "script", "Lmihailris/oiscript/Script;");

            compile(source, context);
            checkcast("mihailris/oiscript/runtime/Function");

            pushContext();
            getfield("mihailris/oiscript/Context", "runHandle", "Lmihailris/oiscript/runtime/OiRunHandle;");

            iconst(values.size());
            anewarray();
            for (int i = 0; i < values.size(); i++) {
                methodVisitor.visitInsn(Opcodes.DUP);
                logger.log("dup");
                iconst(i);
                compile(values.get(i), context);
                methodVisitor.visitInsn(Opcodes.AASTORE);
                logger.log("aastore");
            }

            invokeVirtual(CLS_SCRIPT, "execute", DESC_EXECUTE);
            logger.comment("calling function "+source+" END");
        }
        else if (value instanceof BitInverse) {
            BitInverse inverse = (BitInverse) value;
            compileLong(inverse.getValue(), context);
            // javac compiles `~x` as `x ^ -1`
            lconst(-1);
            methodVisitor.visitInsn(Opcodes.LXOR);
            logger.log("lxor");
            return OiType.LONG;
        }
        else if (value instanceof Not) {
            Not not = (Not) value;
            compileCondition(not.getValue(), context);
            Label elseblock = new Label();
            Label end = new Label();
            // there's no an opcode for NOT-operator
            // javac actually compiles NOT-operator as:
            // #1 IFNE #4
            // #2 ICONST_1
            // #3 GOTO #5
            // #4 ICONST_0
            methodVisitor.visitJumpInsn(Opcodes.IFNE, elseblock);
            logger.log("ifne ", elseblock);
            iconst(1);
            jmp(end);
            label(elseblock);
            iconst(0);
            label(end);
            return OiType.BOOL;
        }
        else {
            throw new IllegalStateException(value.getClass().getSimpleName()+" is not supported yet");
        }
        return OiType.OBJECT;
    }

    private void anewarray() {
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        logger.log("anewarray ", "java/lang/Object");
    }

    private OiType negate(OiType type) {
        if (type == OiType.LONG) {
            methodVisitor.visitInsn(Opcodes.LNEG);
            logger.log("lneg");
            return OiType.LONG;
        } else if (type == OiType.DOUBLE) {
            methodVisitor.visitInsn(Opcodes.DNEG);
            logger.log("dneg");
            return OiType.DOUBLE;
        } else {
            invokeStatic(CLS_ARITHMETICS, "negative", "(Ljava/lang/Object;)Ljava/lang/Object;");
        }
        return type;
    }

    @SuppressWarnings("SameParameterValue")
    private void getstatic(String owner, String name, String descriptor) {
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, owner, name, descriptor);
        logger.log("getstatic ", owner+"."+name+":"+descriptor);
    }

    @SuppressWarnings("SameParameterValue")
    private void getfield(String owner, String name, String descriptor) {
        methodVisitor.visitFieldInsn(Opcodes.GETFIELD, owner, name, descriptor);
        logger.log("getfield ", owner+"."+name+":"+descriptor);
    }

    private void checkcast(String cls) {
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, cls);
        logger.log("checkcast ", cls);
    }

    private void cast(OiType a, OiType b) {
        if (a == OiType.DOUBLE && b == OiType.LONG) {
            methodVisitor.visitInsn(Opcodes.L2D);
            logger.log("l2d");
        }
    }

    private OiType binaryOperation(String operator, OiType tleft, OiType tright) {
        if (tleft == OiType.LONG && tright == OiType.LONG) {
            String opname;
            int opcode;
            switch (operator) {
                case "+": opcode = Opcodes.LADD; opname = "ladd"; break;
                case "-": opcode = Opcodes.LSUB; opname = "lsub"; break;
                case "*": opcode = Opcodes.LMUL; opname = "lmul"; break;
                case "/": opcode = Opcodes.LDIV; opname = "ldiv"; break;
                case "%": opcode = Opcodes.LREM; opname = "lrem"; break;
                case "&": opcode = Opcodes.LAND; opname = "land"; break;
                case "|": opcode = Opcodes.LOR; opname = "lor"; break;
                case "^": opcode = Opcodes.LXOR; opname = "lxor"; break;
                case "**":
                    invokeStatic(CLS_ARITHMETICS, "pow", "(JJ)J");
                    return OiType.LONG;
                default:
                    throw new IllegalStateException("not implemented for "+operator);
            }
            methodVisitor.visitInsn(opcode);
            logger.log(opname);
            return OiType.LONG;
        }

        if (tleft == OiType.DOUBLE || tright == OiType.DOUBLE) {
            String opname;
            int opcode;
            switch (operator) {
                case "+": opcode = Opcodes.DADD; opname = "dadd"; break;
                case "-": opcode = Opcodes.DSUB; opname = "dsub"; break;
                case "*": opcode = Opcodes.DMUL; opname = "dmul"; break;
                case "/": opcode = Opcodes.DDIV; opname = "ddiv"; break;
                case "%": opcode = Opcodes.DREM; opname = "drem"; break;
                case "**":
                    invokeStatic(CLS_MATH, "pow", "(DD)D");
                    return OiType.DOUBLE;
                default:
                    throw new IllegalStateException("not implemented for "+operator);
            }
            methodVisitor.visitInsn(opcode);
            logger.log(opname);
            return OiType.DOUBLE;
        }
        String method;
        String descriptor = DESC_LOGICS;
        if (Operators.isBooleanOperator(operator)) {
            switch (operator) {
                case ">": method = "greather"; break;
                case "<": method = "less";  break;
                case "==": method = "equals"; break;
                case ">=": method = "gequals"; break;
                case "<=": method = "lequals"; break;
                case "in": method = "in"; descriptor = "(Ljava/lang/Object;Ljava/lang/Object;)Z"; break;
                default:
                    throw new IllegalStateException("boolean operator '"+operator+"' is not supported yet");
            }
            invokeStatic(CLS_LOGICS, method, descriptor);
            return OiType.BOOL;
        }
        switch (operator) {
            case "+": method = "add"; break;
            case "-": method = "subtract"; break;
            case "*": method = "multiply"; break;
            case "/": method = "divide"; break;
            case "%": method = "modulo"; break;
            case "**": method = "power"; break;
            default:
                throw new IllegalStateException("operator '"+operator+"' is not supported yet");
        }
        invokeStatic(CLS_ARITHMETICS, method, DESC_ARITHMETICS);
        return OiType.OBJECT;
    }

    private void iconst(int value) {
        if (value >= 0 && value <= 5) {
            methodVisitor.visitInsn(Opcodes.ICONST_0 + value);
            logger.log("iconst_"+value);
        } else {
            int opcode;
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                opcode = Opcodes.BIPUSH;
                logger.log("bipush ", value);
            } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                opcode = Opcodes.SIPUSH;
                logger.log("sipush ", value);
            } else {
                methodVisitor.visitLdcInsn(value);
                logger.log("ldc ", value);
                return;
            }
            methodVisitor.visitIntInsn(opcode, value);
        }
    }

    private void lconst(long value) {
        if (value >= 0 && value <= 1) {
            methodVisitor.visitInsn((int) (Opcodes.LCONST_0 + value));
            logger.log("lconst_", value);
        } else {
            methodVisitor.visitLdcInsn(value);
            logger.log("ldc2_w ", value);
        }
    }

    private void dconst(double value) {
        methodVisitor.visitLdcInsn(value);
        logger.log("ldc2_w ", value);
    }

    private void ldc(Object value) {
        methodVisitor.visitLdcInsn(value);
        logger.log("ldc ", value);
    }

    private void loadThis() {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        logger.log("aload ", "0 // this");
    }

    private void pushNone() {
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "mihailris/oiscript/OiNone", "NONE", "Lmihailris/oiscript/OiNone;");
        logger.log("getstatic ", "mihailris/oiscript/OiNone.NONE // push none");
    }

    private void returnNone() {
        pushNone();
        methodVisitor.visitInsn(Opcodes.ARETURN);
        logger.log("areturn");
    }

    private void pop() {
        methodVisitor.visitInsn(Opcodes.POP);
        logger.log("pop");
    }

    private void pushContext() {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
        logger.log("aload ", "1 // context");
    }
}
