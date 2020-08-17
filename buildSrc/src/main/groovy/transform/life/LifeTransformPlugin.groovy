package transform.life

import com.android.build.api.transform.Transform
import org.gradle.api.Project
import transform.base.BaseTransformPlugin

/**
 * 监听Activity的onCreate执行
 */
class LifeTransformPlugin extends BaseTransformPlugin {

    @Override
    Transform getCustomTransform(Project project) {
        return new LifeTransform()
    }
}
