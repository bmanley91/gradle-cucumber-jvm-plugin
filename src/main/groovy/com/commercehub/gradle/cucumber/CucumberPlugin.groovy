package com.commercehub.gradle.cucumber

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet

/**
 * This is the main plugin file. Put a description of your plugin here.
 */
class CucumberPlugin implements Plugin<Project> {

    public static final String DEFAULT_PARENT_SOURCESET = 'main'

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        project.extensions.create('cucumber', CucumberExtension, project)
        project.metaClass.addCucumberSuite = { String sourceSetName ->
            SourceSet cucumberSuiteSourceSet =
                    project.sourceSets.findByName(sourceSetName) ?: project.sourceSets.create(sourceSetName) {
                        compileClasspath += project.sourceSets[DEFAULT_PARENT_SOURCESET].output
                        compileClasspath += project.sourceSets[DEFAULT_PARENT_SOURCESET].compileClasspath
                        runtimeClasspath = it.output + it.compileClasspath
                    }
            project.tasks.create(name: sourceSetName, type: CucumberTask,
                    dependsOn: cucumberSuiteSourceSet.classesTaskName) {
                sourceSet cucumberSuiteSourceSet
            }
        }
    }
}
