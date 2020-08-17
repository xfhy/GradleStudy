package transform.life

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import transform.base.BaseTransform

class LifeTransform extends BaseTransform {
    @Override
    ClassVisitor getClassVisitor(ClassWriter classWriter) {
        return new LifeClassVisitor(classWriter)
    }

    @Override
    boolean isNeedTraceClass(File file) {
        def name = file.name
        if (!name.endsWith(".class")
                || name.startsWith("R.class")
                || name.startsWith('R$')) {
            return false
        }
        return true
    }
}