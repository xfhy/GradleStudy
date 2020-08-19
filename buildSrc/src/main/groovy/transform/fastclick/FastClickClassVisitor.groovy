package transform.fastclick


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * 防止快速点击
 */
class FastClickClassVisitor extends ClassVisitor {

    FastClickClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "onClick" && descriptor == "(Landroid/view/View;)V") {
            return new FastMethodVisitor(api, methodVisitor, access, name, descriptor)
        } else {
            return methodVisitor
        }
    }

    class FastMethodVisitor extends AdviceAdapter {

        FastMethodVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
            super(api, methodVisitor, access, name, descriptor)
        }

        //方法进入
        @Override
        protected void onMethodEnter() {
            super.onMethodEnter()
            mv.visitMethodInsn(INVOKESTATIC, "com/xfhy/gradledemo/FastClickUtil", "isFastDoubleClick", "()Z", false)
            Label label = new Label()
            mv.visitJumpInsn(IFEQ, label)
            mv.visitInsn(RETURN)
            mv.visitLabel(label)
        }

/*
        利用ASM Bytecode outline 插件生成的ASM代码如下(其中Label部分可以忽略):
        {

        }
         */

        /*
        插入之后的代码如下:
         public void onCreate(Bundle savedInstanceState) {

        }
         */

    }

}
