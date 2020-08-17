package transform.life

import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import transform.methodtime.MethodTrack

/**
 * 监听Activity的onCreate方法执行
 */
class LifeClassVisitor extends ClassVisitor {

    /**
     * 当前类名称
     */
    private String className
    /**
     * 父类名称
     */
    private String superName
    /**
     * 当前class是否需要插桩
     */
    private boolean traceClass = false

    LifeClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        this.superName = superName
        if ("androidx/appcompat/app/AppCompatActivity" == superName) {
            traceClass = true
        }
        println("className = $className , superName = $superName")
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        return new TraceMethodVisitor(api, methodVisitor, access, name, descriptor, className, traceClass)
    }

    class TraceMethodVisitor extends AdviceAdapter {

        private String className
        private String methodName
        private int timeLocalIndex
        private boolean traceClass

        TraceMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, String className, boolean
                traceClass) {
            super(api, methodVisitor, access, name, descriptor)
            String[] path = className.split("/")
            this.className = path[path.length - 1]
            this.methodName = name
            this.traceClass = traceClass
        }

        //todo xfhy 未完成 现在是每个方法都打印了log,,,需要在onCreate里面打

        //方法进入
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter()
            if (!traceClass) {
                return
            }
            mv.visitLdcInsn(className);
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitLdcInsn("-------> onCreate : ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            //mv.visitInsn(POP)
        }

        //方法结束
        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode)
            if (!traceClass) {
                return
            }
        }

        /*
        利用ASM Bytecode outline 插件生成的ASM代码如下(其中Label部分可以忽略):
        {

        }
         */

        /*
        插入之后的代码如下:
         public void delete() {
            long currentTimeMillis = System.currentTimeMillis();
            ......
            long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
            Log.d("Test", "⇢ " + "delete()V: " + currentTimeMillis2 + "ms");
        }
         */

    }

}
