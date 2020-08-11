package transform.methodtime

import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class MethodTraceUtil {

    /***
     * 处理单个文件
     * @param inputFile 输入
     * @param outputFile 输出
     */
    public static void traceFile(File inputFile, File outputFile) {
        if (isNeedTraceClass(inputFile)) {
            FileInputStream inputStream = new FileInputStream(inputFile)
            FileOutputStream outputStream = new FileOutputStream(outputFile)

            ClassReader classReader = new ClassReader(inputStream)
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES)
            TraceClassVisitor adapter = new TraceClassVisitor(classWriter)
            classReader.accept(adapter, 0)
            outputStream.write(classWriter.toByteArray())

            inputStream.close()
            outputStream.close()
        } else {
            FileUtils.copyFile(inputFile, outputFile)
        }
    }

    private static boolean isNeedTraceClass(File file) {
        def name = file.name
        if (!name.endsWith(".class")
                || name.startsWith("R.class")
                || name.startsWith('R$')) {
            return false
        }
        return true
    }

}



