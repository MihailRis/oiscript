package mihailris.oiscript.jit;

import mihailris.oiscript.OiNone;
import mihailris.oiscript.Operators;
import mihailris.oiscript.RawFunction;
import mihailris.oiscript.parsing.*;
import mihailris.oiscript.runtime.OiExecutable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class JitCompiler extends ClassLoader {
    private final JitLogger logger = new JitLogger();

    public OiExecutable compile(RawFunction function) throws InstantiationException, IllegalAccessException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String className = "oiscript_jit/Function$"+function.hashCode();
        writer.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC,
                className,
                null,
                "java/lang/Object",
                new String[]{"mihailris/oiscript/runtime/OiExecutable"});

        // Constructor
        {
            MethodVisitor methodVisitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(Opcodes.RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }

        List<Command> commands = function.getCommands();
        CompilerContext context = new CompilerContext(function);
        // Execute method
        {
            MethodVisitor methodVisitor = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "execute",
                    "(Lmihailris/oiscript/Context;[Ljava/lang/Object;)Ljava/lang/Object;",
                    null,
                    null);
            methodVisitor.visitCode();

            for (Command command : commands) {
                compile(command, context, methodVisitor);
            }
            returnNone(methodVisitor);

            methodVisitor.visitMaxs(3, context.newLocal());
            methodVisitor.visitEnd();
        }

        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Class<?> generatedClass = defineClass(className.replaceAll("/", "."), bytes);
        return (OiExecutable) generatedClass.newInstance();
    }

    private void returnNone(MethodVisitor methodVisitor) {
        pushNone(methodVisitor);
        methodVisitor.visitInsn(Opcodes.ARETURN);
        logger.log("areturn");
    }

    private void compile(Command command, CompilerContext context, MethodVisitor methodVisitor) {
        if (command instanceof Return) {
            Return ret = (Return) command;
            Value value = ret.getValue();
            if (value == null) {
                returnNone(methodVisitor);
                return;
            } else {
                compile(ret.getValue(), context, methodVisitor);
            }
            methodVisitor.visitInsn(Opcodes.ARETURN);
            logger.log("areturn");
        } else if (command instanceof LocalAssignment) {
            LocalAssignment assignment = (LocalAssignment) command;
            Value value = assignment.getValue();
            int index = assignment.getIndex();

            String operator = assignment.getOperator();
            if (operator.equals("=")) {
                astore(methodVisitor, context, index, value);
            } else {
                astore(methodVisitor, context, index, () -> {
                    aload(methodVisitor, context, index);
                    OiType tright = compileTyped(value, context, methodVisitor);
                    boxValue(tright, methodVisitor);
                    OiType type = binaryOperation(methodVisitor, context, operator.substring(0, operator.length()-1), OiType.OBJECT, tright);
                    boxValue(type, methodVisitor);
                });
            }
        } else if (command instanceof While) {
            While loop = (While) command;
            Value condition = loop.getCondition();
            Label start = new Label();
            Label end = new Label();
            label(methodVisitor, start);
            compileCondition(condition, context, methodVisitor);
            ifeq(methodVisitor, end);

            CompilerContext loopContext = context.loop(start, end);
            for (Command subcommand : loop.getCommands()) {
                compile(subcommand, loopContext, methodVisitor);
            }
            jmp(methodVisitor, start);
            label(methodVisitor, end);
        } else if (command instanceof ForLoop) {
            ForLoop forLoop = (ForLoop) command;
            int index = forLoop.getIndex();
            Value value = forLoop.getIterable();
            int iteratorIndex = context.newLocal();
            compile(value, context, methodVisitor);
            astore(methodVisitor, context, iteratorIndex, () -> {
                invokeStatic(methodVisitor, "mihailris/oiscript/OiUtils", "iterator", "(Ljava/lang/Object;)Ljava/util/Iterator;");
            });
            Label start = new Label();
            Label end = new Label();
            label(methodVisitor, start);
            aload(methodVisitor, context, iteratorIndex);
            invokeInterface(methodVisitor, "java/util/Iterator", "hasNext", "()Z");
            ifeq(methodVisitor, end);

            aload(methodVisitor, context, iteratorIndex);

            astore(methodVisitor, context, index, () ->
                    invokeInterface(methodVisitor, "java/util/Iterator", "next", "()Ljava/lang/Object;"));

            CompilerContext loopContext = context.loop(start, end);
            for (Command subcommand : forLoop.getCommands()) {
                compile(subcommand, loopContext, methodVisitor);
            }
            jmp(methodVisitor, start);

            label(methodVisitor, end);
        } else if (command instanceof If) {
            If branch = (If) command;
            Value condition = branch.getCondition();
            List<Elif> elifs = branch.getElifs();
            Else elseblock = branch.getElseBlock();
            Label end = new Label();
            Label next = new Label();

            compileCondition(condition, context, methodVisitor);
            ifeq(methodVisitor, next);
            for (Command subcommand : branch.getCommands()) {
                compile(subcommand, context, methodVisitor);
            }
            if (elseblock != null || !elifs.isEmpty()) {
                jmp(methodVisitor, end);
            }
            label(methodVisitor, next);

            for (int i = 0; i < elifs.size(); i++) {
                Elif elif = elifs.get(i);
                next = new Label();
                compileCondition(elif.getCondition(), context, methodVisitor);
                ifeq(methodVisitor, next);

                for (Command subcommand : elif.getCommands()) {
                    compile(subcommand, context, methodVisitor);
                }
                if (elseblock != null || i+1 < elifs.size()) {
                    jmp(methodVisitor, end);
                }
                label(methodVisitor, next);
            }
            if (elseblock != null) {
                for (Command subcommand : elseblock.getCommands()) {
                    compile(subcommand, context, methodVisitor);
                }
            }
            label(methodVisitor, end);
        } else if (command instanceof ItemAssignment) {
            ItemAssignment assignment = (ItemAssignment) command;
            Value source = assignment.getSource();
            Value key = assignment.getKey();
            Value value = assignment.getValue();
            compile(source, context, methodVisitor);
            compileIndex(key, context, methodVisitor);
            compile(value, context, methodVisitor);
            invokeInterface(methodVisitor, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;");
            pop(methodVisitor);
        } else if (command instanceof ValueWrapper) {
            ValueWrapper wrapper = (ValueWrapper) command;
            compile(wrapper.getValue(), context, methodVisitor);
            pop(methodVisitor);
        } else if (command instanceof Print) {
            Print print = (Print) command;
            Value value = print.getValue();
            getstatic(methodVisitor, "java/lang/System", "out", "Ljava/io/PrintStream;");
            compile(value, context, methodVisitor);
            invokeVirtual(methodVisitor, "java/io/PrintStream", "print", "(Ljava/lang/Object;)V");
        } else if (command instanceof Break) {
            logger.comment("break");
            jmp(methodVisitor, context.getLoopEnd());
        } else if (command instanceof Continue) {
            logger.comment();
        }
        else if (!(command instanceof Pass)){
            throw new IllegalStateException(command.getClass().getSimpleName()+" is not supported yet");
        }
    }

    private void pop(MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(Opcodes.POP);
        logger.log("pop");
    }

    private void getstatic(MethodVisitor methodVisitor, String owner, String name, String descriptor) {
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, owner, name, descriptor);
        logger.log("getstatic ", owner+"."+name+":"+descriptor);
    }

    private void compileIndex(Value value, CompilerContext context, MethodVisitor methodVisitor) {
        compile(value, context, methodVisitor);
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
        logger.log("checkcast java/lang/Number");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
    }

    private void jmp(MethodVisitor methodVisitor, Label label) {
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
        logger.log("goto ", label);
    }

    private void ifeq(MethodVisitor methodVisitor, Label label) {
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label);
        logger.log("ifeq ", label);
    }

    private void compileCondition(Value condition, CompilerContext context, MethodVisitor methodVisitor) {
        OiType type = compileTyped(condition, context, methodVisitor);
        if (type != OiType.BOOL) {
            boxValue(type, methodVisitor);
            invokeStatic(methodVisitor, "mihailris/oiscript/Logics", "isTrue", "(Ljava/lang/Object;)Z");
        }
    }

    private void label(MethodVisitor methodVisitor, Label label) {
        methodVisitor.visitLabel(label);
        logger.log(label);
    }

    private void invokeInterface(MethodVisitor methodVisitor, String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true);
        logger.log("invokeinterface ", owner+"."+name+":"+descriptor);
    }

    private void invokeVirtual(MethodVisitor methodVisitor, String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false);
        logger.log("invokevirtual ", owner+"."+name+":"+descriptor);
    }

    private void invokeStatic(MethodVisitor methodVisitor, String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, descriptor, false);
        logger.log("invokestatic ", owner+"."+name+":"+descriptor);
    }

    private void aload(MethodVisitor methodVisitor, CompilerContext context, int index) {
        int argc = context.getFunction().getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            logger.log("aload 2");
            iconst(methodVisitor, index);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            logger.log("aaload");
        } else {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, index - argc + 3);
            logger.log("aload ", (index - argc+3));
        }
    }

    private void astore(MethodVisitor methodVisitor, CompilerContext context, int index, Runnable value) {
        int argc = context.getFunction().getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            logger.log("aload 2");
            iconst(methodVisitor, index);
            value.run();
            methodVisitor.visitInsn(Opcodes.AASTORE);
            logger.log("aastore");
        } else {
            value.run();
            methodVisitor.visitVarInsn(Opcodes.ASTORE, index - argc + 3);
            logger.log("astore ", (index - argc+3));
        }
    }

    private void astore(MethodVisitor methodVisitor, CompilerContext context, int index, Value value) {
        int argc = context.getFunction().getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            logger.log("aload 2");
            iconst(methodVisitor, index);
            compile(value, context, methodVisitor);
            methodVisitor.visitInsn(Opcodes.AASTORE);
            logger.log("aastore");
        } else {
            compile(value, context, methodVisitor);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, index - argc + 3);
            logger.log("astore ", (index - argc+3));
        }
    }

    private void compile(Value value, CompilerContext context, MethodVisitor methodVisitor) {
        OiType type = compileTyped(value, context, methodVisitor);
        if (type != OiType.OBJECT)
            boxValue(type, methodVisitor);
    }

    private void boxValue(OiType type, MethodVisitor methodVisitor) {
        switch (type) {
            case BOOL: invokeStatic(methodVisitor, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"); break;
            case LONG: invokeStatic(methodVisitor, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"); break;
            case DOUBLE: invokeStatic(methodVisitor, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;"); break;
        }
    }

    private OiType compileTyped(Value value, CompilerContext context, MethodVisitor methodVisitor) {
        value = value.optimize();
        if (value instanceof BinaryOperator) {
            BinaryOperator operator = (BinaryOperator) value;
            Value left = operator.getLeft();
            Value right = operator.getRight();
            OiType tleft = left.getType();
            OiType tright = right.getType();

            compileTyped(left, context, methodVisitor);
            if (tleft == OiType.OBJECT || tright == OiType.OBJECT)
                boxValue(tleft, methodVisitor);
            if (tleft == OiType.DOUBLE || tright == OiType.DOUBLE)
                cast(tright, tleft, methodVisitor);

            compileTyped(right, context, methodVisitor);
            if (tleft == OiType.OBJECT || tright == OiType.OBJECT)
                boxValue(tright, methodVisitor);
            if (tleft == OiType.DOUBLE)
                cast(tright, tleft, methodVisitor);
            return binaryOperation(methodVisitor, context, operator.getOperator(), tleft, tright);
        } else if (value instanceof LocalName) {
            LocalName variable = (LocalName) value;
            int index = variable.getIndex();
            aload(methodVisitor, context, index);
        } else if (value instanceof IntegerValue) {
            IntegerValue integerValue = (IntegerValue) value;
            lconst(methodVisitor, integerValue.getValue());
            return OiType.LONG;
        } else if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            iconst(methodVisitor, booleanValue.getValue() ? 1 : 0);
            return OiType.BOOL;
        } else if (value instanceof NumberValue) {
            NumberValue numberValue = (NumberValue) value;
            dconst(methodVisitor, numberValue.getValue());
            return OiType.DOUBLE;
        } else if (value instanceof OiNone) {
            pushNone(methodVisitor);
        } else if (value instanceof ListValue) {
            logger.comment("list literal");
            ListValue listValue = (ListValue) value;
            List<Value> values = listValue.getValues();
            iconst(methodVisitor, values.size());
            methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            logger.log("anewarray java/lang/Object");
            for (int i = 0; i < values.size(); i++) {
                methodVisitor.visitInsn(Opcodes.DUP);
                logger.log("dup");
                iconst(methodVisitor, i);
                compile(values.get(i), context, methodVisitor);
                methodVisitor.visitInsn(Opcodes.AASTORE);
                logger.log("aastore");
            }
            invokeStatic(methodVisitor, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
        } else if (value instanceof StringValue) {
            StringValue stringValue = (StringValue) value;
            ldc(methodVisitor, stringValue.getValue());
        }
        else if (value instanceof Negative) {
            Negative negative = (Negative) value;
            OiType type = compileTyped(negative.getValue(), context, methodVisitor);
            if (type == OiType.LONG) {
                methodVisitor.visitInsn(Opcodes.LNEG);
                logger.log("lneg");
                return OiType.LONG;
            } else if (type == OiType.DOUBLE) {
                methodVisitor.visitInsn(Opcodes.DNEG);
                logger.log("dneg");
                return OiType.DOUBLE;
            } else {
                invokeStatic(methodVisitor, CLS_ARITHMETICS, "negative", "(Ljava/lang/Object;)Ljava/lang/Object;");
            }
        }
        else if (value instanceof ItemValue) {
            ItemValue itemValue = (ItemValue) value;
            compile(itemValue.getSource(), context, methodVisitor);
            compileIndex(itemValue.getKey(), context, methodVisitor);
            invokeInterface(methodVisitor, "java/util/List", "get", "(I)Ljava/lang/Object;");
        }
        else if (value instanceof AttributeValue) {
            AttributeValue attributeValue = (AttributeValue) value;
            Value source = attributeValue.getSource();
            String name = attributeValue.getName();

            compile(source, context, methodVisitor);
            if (name.equals("len")) {
                invokeStatic(methodVisitor, "mihailris/oiscript/OiUtils", "length", "(Ljava/lang/Object;)J");
                return OiType.LONG;
            }
            throw new IllegalStateException("not implemented for "+source+"->"+name);
        }
        else if (value instanceof NamedValue) {
            NamedValue namedValue = (NamedValue) value;
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            logger.log("aload 1");
            String name = namedValue.getName();
            ldc(methodVisitor, name);
            invokeVirtual(methodVisitor, "mihailris/oiscript/Context", "get", "(Ljava/lang/String;)Ljava/lang/Object;");
        }
        else if (value instanceof Call) {
            Call call = (Call) value;
            Value source = call.getSource();
            List<Value> values = call.getValues();

            logger.comment("calling function "+source);

            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            logger.log("aload 1");
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "mihailris/oiscript/Context", "script", "Lmihailris/oiscript/Script;");
            logger.log("getfield ", "mihailris/oiscript/Context.script");

            compile(source, context, methodVisitor);
            methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "mihailris/oiscript/runtime/Function");
            logger.log("checkcast ", "mihailris/oiscript/runtime/Function");

            methodVisitor.visitVarInsn(Opcodes.ALOAD, 1);
            logger.log("aload 1");
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "mihailris/oiscript/Context", "runHandle", "Lmihailris/oiscript/runtime/OiRunHandle;");
            logger.log("getfield ", "mihailris/oiscript/Context.runHandle");

            iconst(methodVisitor, values.size());
            methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            logger.log("anewarray java/lang/Object");
            for (int i = 0; i < values.size(); i++) {
                methodVisitor.visitInsn(Opcodes.DUP);
                logger.log("dup");
                iconst(methodVisitor, i);
                compile(values.get(i), context, methodVisitor);
                methodVisitor.visitInsn(Opcodes.AASTORE);
                logger.log("aastore");
            }

            invokeVirtual(methodVisitor, "mihailris/oiscript/Script", "execute", "(Lmihailris/oiscript/runtime/Function;Lmihailris/oiscript/runtime/OiRunHandle;[Ljava/lang/Object;)Ljava/lang/Object;");
        }
        else {
            throw new IllegalStateException(value.getClass().getSimpleName()+" is not supported yet");
        }
        return OiType.OBJECT;
    }

    private void pushNone(MethodVisitor methodVisitor) {
        logger.comment("push none");
        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "mihailris/oiscript/OiNone", "NONE", "Lmihailris/oiscript/OiNone;");
        logger.log("getstatic mihailris/oiscript/OiNone.NONE");
    }

    private void cast(OiType a, OiType b, MethodVisitor methodVisitor) {
        if (a == OiType.DOUBLE && b == OiType.LONG) {
            methodVisitor.visitInsn(Opcodes.L2D);
            logger.log("l2d");
        }
    }

    private static final String CLS_ARITHMETICS = "mihailris/oiscript/Arithmetics";
    private static final String CLS_LOGICS = "mihailris/oiscript/Logics";
    private static final String DESC_ARITHMETICS = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String DESC_LOGICS = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
    private OiType binaryOperation(MethodVisitor methodVisitor, CompilerContext context, String operator, OiType tleft, OiType tright) {
        if (tleft == OiType.LONG && tright == OiType.LONG) {
            String opname;
            int opcode;
            switch (operator) {
                case "+": opcode = Opcodes.LADD; opname = "ladd"; break;
                case "-": opcode = Opcodes.LSUB; opname = "lsub"; break;
                case "*": opcode = Opcodes.LMUL; opname = "lmul"; break;
                case "/": opcode = Opcodes.LDIV; opname = "ldiv"; break;
                case "%": opcode = Opcodes.LREM; opname = "lrem"; break;
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
            invokeStatic(methodVisitor, CLS_LOGICS, method, descriptor);
            return OiType.BOOL;
        }
        switch (operator) {
            case "+": method = "add"; break;
            case "-": method = "subtract"; break;
            case "*": method = "multiply"; break;
            case "/": method = "divide"; break;
            default:
                throw new IllegalStateException("operator '"+operator+"' is not supported yet");
        }
        invokeStatic(methodVisitor, CLS_ARITHMETICS, method, DESC_ARITHMETICS);
        return OiType.OBJECT;
    }

    private void iconst(MethodVisitor methodVisitor, int value) {
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

    private void lconst(MethodVisitor methodVisitor, long value) {
        if (value >= 0 && value <= 1) {
            methodVisitor.visitInsn((int) (Opcodes.LCONST_0 + value));
            logger.log("lconst_", value);
        } else {
            methodVisitor.visitLdcInsn(value);
            logger.log("ldc2_w ", value);
        }
    }

    private void dconst(MethodVisitor methodVisitor, double value) {
        methodVisitor.visitLdcInsn(value);
        logger.log("ldc2_w ", value);
    }

    private void ldc(MethodVisitor methodVisitor, Object value) {
        methodVisitor.visitLdcInsn(value);
        logger.log("ldc ", value);
    }

    private Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
