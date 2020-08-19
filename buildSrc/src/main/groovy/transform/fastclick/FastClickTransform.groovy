package transform.fastclick

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import transform.base.BaseTransform
import transform.hello.HelloClassVisitor

class FastClickTransform extends BaseTransform {

    @Override
    String getName() {
        return "FastClickTransform"
    }

    @Override
    ClassVisitor getClassVisitor(ClassWriter classWriter) {
        return new FastClickClassVisitor(classWriter)
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