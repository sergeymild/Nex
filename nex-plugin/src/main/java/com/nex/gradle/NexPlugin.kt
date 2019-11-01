package com.nex.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The plugin.
 */
open class NexPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val android = target.extensions.findByName("android") as BaseExtension
        android.registerTransform(
            NexTransformer(
                target
            )
        )
    }

}
