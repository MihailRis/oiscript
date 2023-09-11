package mihailris.oiscript.jit;

import mihailris.oiscript.RawFunction;
import mihailris.oiscript.runtime.OiExecutable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JitCompiler extends ClassLoader {
    private static final String DESC_EXECUTE = "(Lmihailris/oiscript/Context;[Ljava/lang/Object;)Ljava/lang/Object;";
    public OiExecutable compile(String source, RawFunction function) throws InstantiationException, IllegalAccessException {
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

        compileFunction(className, writer, function);

        writer.visitSource(source, null);
        writer.visitEnd();
        byte[] bytes = writer.toByteArray();

        Class<?> generatedClass = defineClass(className.replaceAll("/", "."), bytes);
        return (OiExecutable) generatedClass.newInstance();
    }

    private void compileFunction(String className, ClassWriter writer, RawFunction function) {
        CompilerContext context = new CompilerContext(function);
        String methodName = "$"+function.getName();
        int argc = function.getArgs().size();

        StringBuilder descriptor = new StringBuilder("(Lmihailris/oiscript/Context;");
        for (int i = 0; i < argc; i++) {
            descriptor.append("Ljava/lang/Object;");
        }
        descriptor.append(")Ljava/lang/Object;");

        {
            MethodVisitor methodVisitor = writer.visitMethod(
                    Opcodes.ACC_PRIVATE,
                    methodName,
                    descriptor.toString(),
                    null,
                    null
            );

            methodVisitor.visitCode();
            new JitFunctionCompiler(function, methodVisitor).compileFunction(context, methodName, descriptor.toString());
            methodVisitor.visitMaxs(3, context.newLocal());
            methodVisitor.visitEnd();
        }

        // Execute method
        {
            MethodVisitor methodVisitor = writer.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "execute",
                    DESC_EXECUTE,
                    null,
                    null);
            methodVisitor.visitCode();

            new JitFunctionCompiler(function, methodVisitor).compileAdapter(className, methodName, descriptor.toString());

            methodVisitor.visitMaxs(3, 5);
            methodVisitor.visitEnd();
        }
    }

    private Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
