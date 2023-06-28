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

public class  JitCompiler extends ClassLoader {
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
            methodVisitor.visitMaxs(3, context.newLocal());
            methodVisitor.visitEnd();
        }

        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Class<?> generatedClass = defineClass(className.replaceAll("/", "."), bytes);
        return (OiExecutable) generatedClass.newInstance();
    }

    private void compile(Command command, CompilerContext context, MethodVisitor methodVisitor) {
        if (command instanceof Return) {
            Return ret = (Return) command;
            Value value = ret.getValue();
            if (value == null) {
                methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "mihailris/oiscript/OiNone", "NONE", "Lmihailris/oiscript/OiNone;");
                log("getstatic mihailris/oiscript/OiNone.NONE");
            } else {
                compile(ret.getValue(), context, methodVisitor);
            }
            methodVisitor.visitInsn(Opcodes.ARETURN);
            log("areturn");
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

            for (Command subcommand : loop.getCommands()) {
                compile(subcommand, context, methodVisitor);
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

            for (Command subcommand : forLoop.getCommands()) {
                compile(subcommand, context, methodVisitor);
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
            methodVisitor.visitInsn(Opcodes.POP);
            log("pop");
        }
        else if (!(command instanceof Pass)){
            throw new IllegalStateException(command.getClass().getSimpleName()+" is not supported yet");
        }
    }

    private void compileIndex(Value value, CompilerContext context, MethodVisitor methodVisitor) {
        compile(value, context, methodVisitor);
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
        log("checkcast java/lang/Number");
        methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
    }

    private void jmp(MethodVisitor methodVisitor, Label label) {
        methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
        log("goto ", label);
    }

    private void ifeq(MethodVisitor methodVisitor, Label label) {
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, label);
        log("ifeq ", label);
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
        log(label);
    }

    private void invokeInterface(MethodVisitor methodVisitor, String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, name, descriptor, true);
        log("invokeinterface ", owner+"."+name+":"+descriptor);
    }

    private void invokeStatic(MethodVisitor methodVisitor, String owner, String name, String descriptor) {
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, descriptor, false);
        log("invokestatic ", owner+"."+name+":"+descriptor);
    }

    private void aload(MethodVisitor methodVisitor, CompilerContext context, int index) {
        int argc = context.getFunction().getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            log("aload 2");
            iconst(methodVisitor, index);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            log("aaload");
        } else {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, index - argc + 3);
            log("aload ", (index - argc+3));
        }
    }

    private void astore(MethodVisitor methodVisitor, CompilerContext context, int index, Runnable value) {
        int argc = context.getFunction().getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            log("aload 2");
            iconst(methodVisitor, index);
            value.run();
            methodVisitor.visitInsn(Opcodes.AASTORE);
            log("aastore");
        } else {
            value.run();
            methodVisitor.visitVarInsn(Opcodes.ASTORE, index - argc + 3);
            log("astore ", (index - argc+3));
        }
    }

    private void log(String text) {
        System.out.print(";  ");
        System.out.println(text);
    }

    private void log(String text, Object arg) {
        System.out.print(";  ");
        System.out.print(text);
        System.out.println(arg);
    }

    private void log(Label label) {
        System.out.println("."+label+":");
    }

    private void astore(MethodVisitor methodVisitor, CompilerContext context, int index, Value value) {
        int argc = context.getFunction().getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            log("aload 2");
            iconst(methodVisitor, index);
            compile(value, context, methodVisitor);
            methodVisitor.visitInsn(Opcodes.AASTORE);
            log("aastore");
        } else {
            compile(value, context, methodVisitor);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, index - argc + 3);
            log("astore ", (index - argc+3));
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
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "mihailris/oiscript/OiNone", "NONE", "Lmihailris/oiscript/OiNone;");
            log("getstatic mihailris/oiscript/OiNone.NONE");
        } else if (value instanceof ListValue) {
            ListValue listValue = (ListValue) value;
            List<Value> values = listValue.getValues();
            iconst(methodVisitor, values.size());
            methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            log("anewarray java/lang/Object");
            for (int i = 0; i < values.size(); i++) {
                methodVisitor.visitInsn(Opcodes.DUP);
                log("dup");
                iconst(methodVisitor, i);
                compile(values.get(i), context, methodVisitor);
                methodVisitor.visitInsn(Opcodes.AASTORE);
                log("aastore");
            }
            invokeStatic(methodVisitor, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
        } else if (value instanceof StringValue) {
            StringValue stringValue = (StringValue) value;
            ldc(methodVisitor, stringValue.getValue());
        }
        else if (value instanceof Negative) {
            Negative negative = (Negative) value;
            compile(negative.getValue(), context, methodVisitor);
            invokeStatic(methodVisitor, CLS_ARITHMETICS, "negative", "(Ljava/lang/Object;)Ljava/lang/Object;");
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
        }
        else {
            throw new IllegalStateException(value.getClass().getSimpleName()+" is not supported yet");
        }
        return OiType.OBJECT;
    }

    private void cast(OiType a, OiType b, MethodVisitor methodVisitor) {
        if (a == OiType.DOUBLE && b == OiType.LONG) {
            methodVisitor.visitInsn(Opcodes.L2D);
            log("l2d");
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
            log(opname);
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
            log(opname);
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
            log("iconst_"+value);
        } else {
            int opcode;
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                opcode = Opcodes.BIPUSH;
                log("bipush ", value);
            } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                opcode = Opcodes.SIPUSH;
                log("sipush ", value);
            } else {
                methodVisitor.visitLdcInsn(value);
                log("ldc ", value);
                return;
            }
            methodVisitor.visitIntInsn(opcode, value);
        }
    }

    private void lconst(MethodVisitor methodVisitor, long value) {
        if (value >= 0 && value <= 1) {
            methodVisitor.visitInsn((int) (Opcodes.LCONST_0 + value));
            log("lconst_", value);
        } else {
            methodVisitor.visitLdcInsn(value);
            log("ldc2_w ", value);
        }
    }

    private void dconst(MethodVisitor methodVisitor, double value) {
        methodVisitor.visitLdcInsn(value);
        log("ldc2_w ", value);
    }

    private void ldc(MethodVisitor methodVisitor, Object value) {
        methodVisitor.visitLdcInsn(value);
        log("ldc ", value);
    }

    private Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
