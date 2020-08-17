package transform.methodtime

import org.objectweb.asm.Type
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 在方法的前后插入代码,统计方法执行时长.(包含了构造方法)
 */
class TraceClassVisitor extends ClassVisitor {

    private String className
    private boolean traceClass

    TraceClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor)
    }

    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name
        println("className = $className , superName = $superName")
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        return new TraceMethodVisitor(api, methodVisitor, access, name, descriptor, className, traceClass)
    }

    @Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        //类上面的注解 如果描述符和MethodTrack一样  则表示所有方法都需要计算耗时
        if (Type.getDescriptor(MethodTrack.class) == descriptor) {
            traceClass = true
        }
        return super.visitAnnotation(descriptor, visible)
    }

    class TraceMethodVisitor extends AdviceAdapter {

        private String className
        private String methodName
        //标记 当前方法是否需要计算耗时
        private boolean traceCurrentMethod
        //标记 当前类的所有方法都需要计算耗时?
        private boolean traceClassAllMethod
        private int timeLocalIndex

        TraceMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, String className, boolean
                traceClass) {
            super(api, methodVisitor, access, name, descriptor)
            String[] path = className.split("/")
            this.className = path[path.length - 1]
            this.methodName = name
            this.traceClassAllMethod = traceClass
        }

        @Override
        AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            //方法上面的注解 如果和MethodTrack一样  则表示当前方法需要计算耗时
            if (Type.getDescriptor(MethodTrack.class) == descriptor) {
                traceCurrentMethod = true
            }
            return super.visitAnnotation(descriptor, visible)
        }

        //方法进入
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter()
            if (traceClassAllMethod || traceCurrentMethod) {
                //构造一个局部变量
                timeLocalIndex = newLocal(Type.LONG_TYPE)
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
                mv.visitVarInsn(LSTORE, timeLocalIndex)
            }
        }

        //方法结束
        @Override
        protected void onMethodExit(int opcode) {
            super.onMethodExit(opcode)
            if (traceClassAllMethod || traceCurrentMethod) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false)
                mv.visitVarInsn(LLOAD, timeLocalIndex)
                mv.visitInsn(LSUB)
                mv.visitVarInsn(LSTORE, timeLocalIndex)

                mv.visitLdcInsn(className)
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
                mv.visitInsn(DUP)
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
                mv.visitLdcInsn("⇢ ")
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                mv.visitLdcInsn(methodName + methodDesc + ": ")
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                mv.visitVarInsn(LLOAD, timeLocalIndex)
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false)
                mv.visitLdcInsn("ms")
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
                mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false)
                mv.visitInsn(POP)
            }
        }

        /*
        利用ASM Bytecode outline 插件生成的ASM代码如下(其中Label部分可以忽略):
        {
            mv = cw.visitMethod(ACC_PUBLIC, "delete2", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLineNumber(33, l0);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LSTORE, 1);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLineNumber(35, l1);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("xfhy");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLineNumber(37, l2);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitVarInsn(LLOAD, 1);
            mv.visitInsn(LSUB);
            mv.visitVarInsn(LSTORE, 3);
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(38, l3);
            mv.visitLdcInsn("Test");
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
            mv.visitLdcInsn("\u21e2 delete()V: ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitVarInsn(LLOAD, 3);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
            mv.visitLdcInsn("ms");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
            mv.visitInsn(POP);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitLineNumber(39, l4);
            mv.visitInsn(RETURN);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLocalVariable("this", "Lcom/xfhy/gradledemo/Test;", null, l0, l5, 0);
            mv.visitLocalVariable("currentTimeMillis", "J", null, l1, l5, 1);
            mv.visitLocalVariable("currentTimeMillis2", "J", null, l3, l5, 3);
            mv.visitMaxs(4, 5);
            mv.visitEnd();
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
