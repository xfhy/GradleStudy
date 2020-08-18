package transform.hello

import com.android.build.api.transform.Transform
import org.gradle.api.Project
import transform.base.BaseTransformPlugin

class HelloTransformPlugin extends BaseTransformPlugin {

    @Override
    Transform getCustomTransform(Project project) {
        return new HelloTransform()
    }
}
