package com.nex.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The plugin.
 */
open class NexPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.dependencies.add("implementation", "com.nex:nex-library:1.0.17")

        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(
            NexTransformer(
                target
            )
        )
    }

}
