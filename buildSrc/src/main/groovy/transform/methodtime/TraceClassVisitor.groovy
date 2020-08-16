package transform.methodtime

import org.objectweb.asm.Type
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

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

    }

}
