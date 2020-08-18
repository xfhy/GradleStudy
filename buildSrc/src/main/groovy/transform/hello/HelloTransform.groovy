package transform.hello

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import transform.base.BaseTransform

class HelloTransform extends BaseTransform {

    @Override
    String getName() {
        return "LifeTransform"
    }

    @Override
    ClassVisitor getClassVisitor(ClassWriter classWriter) {
        return new HelloClassVisitor(classWriter)
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