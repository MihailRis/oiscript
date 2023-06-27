package mihailris.oiscript.jit;

import mihailris.oiscript.RawFunction;
import mihailris.oiscript.parsing.*;
import mihailris.oiscript.runtime.Function;
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
                compile(command, function, methodVisitor);
            }

            /*methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            methodVisitor.visitInsn(Opcodes.ICONST_0);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            methodVisitor.visitInsn(Opcodes.ARETURN);*/
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }

        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Class<?> generatedClass = defineClass(className.replaceAll("/", "."), bytes);
        return (OiExecutable) generatedClass.newInstance();
    }

    private void compile(Command command, Function function, MethodVisitor methodVisitor) {
        if (command instanceof Return) {
            Return ret = (Return) command;
            Value value = ret.getValue();
            if (value == null) {
                methodVisitor.visitFieldInsn(Opcodes.ACONST_NULL, "mihailris.oiscript.OiNone", "NONE", null);
                log("aconst_null");
            } else {
                compile(ret.getValue(), function, methodVisitor);
            }
            methodVisitor.visitInsn(Opcodes.ARETURN);
            log("areturn");
        } else if (command instanceof LocalAssignment) {
            LocalAssignment assignment = (LocalAssignment) command;
            Value value = assignment.getValue();
            int index = assignment.getIndex();

            String operator = assignment.getOperator();
            if (operator.equals("=")) {
                astore(methodVisitor, function, index, value);
            } else {
                astore(methodVisitor, function, index, () -> {
                    aload(methodVisitor, function, index);
                    compile(value, function, methodVisitor);
                    binaryOperation(methodVisitor, operator.substring(0, operator.length()-1));
                });
            }
        } else if (command instanceof While) {
            While loop = (While) command;
            Value condition = loop.getCondition();
            Label start = new Label();
            Label end = new Label();
            methodVisitor.visitLabel(start);
            log(start);
            compile(condition, function, methodVisitor);
            methodVisitor.visitJumpInsn(Opcodes.IFEQ, end);
            log("ifeq "+end);

            for (Command subcommand : loop.getCommands()) {
                compile(subcommand, function, methodVisitor);
            }
            methodVisitor.visitJumpInsn(Opcodes.GOTO, start);
            log("goto "+start);
            methodVisitor.visitLabel(end);
            log(end);
        } else if (command instanceof Pass) {
        } else {
            throw new IllegalStateException(command.getClass().getSimpleName()+" is not supported yet");
        }
    }

    private void aload(MethodVisitor methodVisitor, Function function, int index) {
        int argc = function.getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            log("aload 2");
            iconst(methodVisitor, index);
            methodVisitor.visitInsn(Opcodes.AALOAD);
            log("aaload");
        } else {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, index - argc + 3);
            log("aload "+(index - argc+3));
        }
    }

    private void astore(MethodVisitor methodVisitor, Function function, int index, Runnable value) {
        int argc = function.getArgs().size();
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
            log("astore "+(index - argc+3));
        }
    }

    private void log(String text) {
        System.out.println(";  " + text);
    }

    private void log(Label label) {
        System.out.println("."+label+":");
    }

    private void astore(MethodVisitor methodVisitor, Function function, int index, Value value) {
        int argc = function.getArgs().size();
        if (index < argc) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 2);
            log("aload 2");
            iconst(methodVisitor, index);
            compile(value, function, methodVisitor);
            methodVisitor.visitInsn(Opcodes.AASTORE);
            log("aastore");
        } else {
            compile(value, function, methodVisitor);
            methodVisitor.visitVarInsn(Opcodes.ASTORE, index - argc + 3);
            log("astore "+(index - argc+3));
        }
    }

    private void compile(Value value, Function function, MethodVisitor methodVisitor) {
        value = value.optimize();
        if (value instanceof BinaryOperator) {
            BinaryOperator operator = (BinaryOperator) value;
            compile(operator.getLeft(), function, methodVisitor);
            compile(operator.getRight(), function, methodVisitor);
            binaryOperation(methodVisitor, operator.getOperator());
        } else if (value instanceof LocalName) {
            LocalName variable = (LocalName) value;
            int index = variable.getIndex();
            aload(methodVisitor, function, index);
        } else if (value instanceof IntegerValue) {
            IntegerValue integerValue = (IntegerValue) value;
            lconst(methodVisitor, integerValue.getValue());
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            log("invokestatic Long.valueOf");
        } else {
            throw new IllegalStateException(value.getClass().getSimpleName()+" is not supported yet");
        }
    }

    private static final String CLS_ARITHMETICS = "mihailris/oiscript/Arithmetics";
    private static final String CLS_LOGICS = "mihailris/oiscript/Logics";
    private static final String DESC_ARITHMETICS = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String DESC_LOGICS = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
    private void binaryOperation(MethodVisitor methodVisitor, String operator) {
        String method;
        String cls = CLS_ARITHMETICS;
        String descriptor = DESC_ARITHMETICS;
        switch (operator) {
            case "+": method = "add"; break;
            case "-": method = "subtract"; break;
            case "*": method = "multiply"; break;
            case ">": method = "greather"; cls = CLS_LOGICS; descriptor = DESC_LOGICS; break;
            case "<": method = "less"; cls = CLS_LOGICS; descriptor = DESC_LOGICS; break;
            default:
                throw new IllegalStateException("operator '"+operator+"' is not supported yet");
        }
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, cls, method, descriptor, false);
        log("invokestatic "+cls+"."+method+":"+descriptor);
    }

    private void iconst(MethodVisitor methodVisitor, int value) {
        if (value >= 0 && value <= 5) {
            methodVisitor.visitInsn(Opcodes.ICONST_0 + value);
            log("iconst_"+value);
        } else {
            int opcode;
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                opcode = Opcodes.BIPUSH;
                log("bipush "+value);
            } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                opcode = Opcodes.SIPUSH;
                log("sipush "+value);
            } else {
                methodVisitor.visitLdcInsn(value);
                log("ldc "+value);
                return;
            }
            methodVisitor.visitIntInsn(opcode, value);
        }
    }

    private void lconst(MethodVisitor methodVisitor, long value) {
        if (value >= 0 && value <= 5) {
            methodVisitor.visitInsn((int) (Opcodes.LCONST_0 + value));
            log("lconst_"+value);
        } else {
            methodVisitor.visitLdcInsn(value);
            log("ldc2_w "+value);
        }
    }

    private Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
