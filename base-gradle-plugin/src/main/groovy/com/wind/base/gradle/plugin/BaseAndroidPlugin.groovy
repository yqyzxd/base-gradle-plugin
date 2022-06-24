package com.wind.base.gradle.plugin
import com.android.build.api.transform.Transform
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BaseAndroidPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        AppExtension android = project.extensions.findByType(AppExtension)
        android.registerTransform(getTransform())
    }

    abstract Transform getTransform()

}