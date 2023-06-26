package mihailris.oiscript.jit;

import mihailris.oiscript.Arithmetics;
import mihailris.oiscript.parsing.*;
import mihailris.oiscript.runtime.Function;
import mihailris.oiscript.runtime.OiExecutable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class JitCompiler extends ClassLoader {
    public OiExecutable compile(Function function) throws InstantiationException, IllegalAccessException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
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
                compile(command, methodVisitor);
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

    private void compile(Command command, MethodVisitor methodVisitor) {
        System.out.println(command.getClass().getSimpleName()+": "+command);
        if (command instanceof Return) {
            Return ret = (Return) command;
            Value value = ret.getValue();
            if (value == null) {
                methodVisitor.visitFieldInsn(Opcodes.ACONST_NULL, "mihailris.oiscript.OiNone", "NONE", null);
            } else {
                compile(ret.getValue(), methodVisitor);
            }
            methodVisitor.visitInsn(Opcodes.ARETURN);
        }
    }

    private void compile(Value value, MethodVisitor methodVisitor) {
        value = value.optimize();
        System.out.println(value.getClass().getSimpleName()+":V: "+value);
        if (value instanceof BinaryOperator) {
            BinaryOperator operator = (BinaryOperator) value;
            compile(operator.getLeft(), methodVisitor);
            compile(operator.getRight(), methodVisitor);
            switch (operator.getOperator()) {
                case "*":
                    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "mihailris/oiscript/Arithmetics", "multiply", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
                    break;
            }
        } else if (value instanceof NamedValue) {
            NamedValue variable = (NamedValue) value;
            String name = variable.getName();
        }
    }

    private Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
