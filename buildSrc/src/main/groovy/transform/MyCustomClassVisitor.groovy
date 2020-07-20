package transform

import jdk.internal.org.objectweb.asm.ClassVisitor

/**
 * @author : xfhy
 * Create time : 2020/7/20 11:09 PM
 * Description : 
 */
class MyCustomClassVisitor extends ClassVisitor {

    MyCustomClassVisitor(int var1) {
        super(var1)
    }

    MyCustomClassVisitor(int var1, ClassVisitor var2) {
        super(var1, var2)
    }

}
