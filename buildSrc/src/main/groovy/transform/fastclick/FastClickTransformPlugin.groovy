package transform.fastclick

import com.android.build.api.transform.Transform
import org.gradle.api.Project
import transform.base.BaseTransformPlugin
import transform.hello.HelloTransform

class FastClickTransformPlugin extends BaseTransformPlugin {

    @Override
    Transform getCustomTransform(Project project) {
        return new FastClickTransform()
    }
}
