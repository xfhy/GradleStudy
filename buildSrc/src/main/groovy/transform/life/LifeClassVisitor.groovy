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
        //println("methodName = $name")
        if ("onCreate" == name) {
            //只有onCreate需要插桩
            return new TraceMethodVisitor(api, methodVisitor, access, name, descriptor, className, traceClass)
        } else {
            return methodVisitor
        }
    }

    class TraceMethodVisitor extends AdviceAdapter {

        private String className
        private String methodName
        private boolean traceClass

        TraceMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, String className, boolean
                traceClass) {
            super(api, methodVisitor, access, name, descriptor)
            String[] path = className.split("/")
            this.className = path[path.length - 1]
            this.methodName = name
            this.traceClass = traceClass
        }

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
            mv.visitInsn(POP)
        }

        /*
        利用ASM Bytecode outline 插件生成的ASM代码如下(其中Label部分可以忽略):
        {
            mv = cw.visitMethod(ACC_PROTECTED, "onCreate", "(Landroid/os/Bundle;)V", null, null);
            {
                av0 = mv.visitParameterAnnotation(0, "Landroidx/annotation/Nullable;", false);
                av0.visitEnd();
            }
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(18, l0);
            mv.visitLdcInsn("TestActivity");
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
            mv.visitInsn(POP);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(19, l1);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "androidx/appcompat/app/AppCompatActivity", "onCreate", "(Landroid/os/Bundle;)V", false);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(20, l2);
            mv.visitInsn(RETURN);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLocalVariable("this", "Lcom/xfhy/gradledemo/TestActivity;", null, l0, l3, 0);
            mv.visitLocalVariable("savedInstanceState", "Landroid/os/Bundle;", null, l0, l3, 1);
            mv.visitMaxs(3, 2);
            mv.visitEnd();
        }
         */

        /*
        插入之后的代码如下:
         public void onCreate(Bundle savedInstanceState) {
            StringBuilder sb = new StringBuilder();
            sb.append("-------> onCreate : ");
            sb.append(getClass().getSimpleName());
            Log.d("TestActivity", sb.toString());
        }
         */

    }

}
