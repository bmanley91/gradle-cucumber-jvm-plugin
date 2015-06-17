package com.commercehub.gradle.cucumber

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

/**
 * Created by jgelais on 6/11/15.
 */
class CucumberTask extends DefaultTask implements CucumberRunnerOptions {
    public static final String CUCUMBER_REPORTS_DIR = 'cucumber'
    public static final String CUCUMBER_EXTENSION_NAME = 'cucumber'

    private SourceSet sourceSet
    private final CucumberExtension extension = project.extensions[CUCUMBER_EXTENSION_NAME]

    List<String> tags = null
    Integer maxParallelForks = null
    String featureRoot = null
    String stepDefinitionRoot = null
    Boolean isDryRun = null
    Boolean isMonochrome = null
    Boolean isStrict = null
    String snippets = null

    @TaskAction
    void runTests() {
        CucumberRunner runner = new CucumberRunner(this)
        runner.run(sourceSet, createResultsDir())
    }

    @SuppressWarnings('ConfusingMethodName')
    def sourceSet(SourceSet sourceSet) {
        this.sourceSet = sourceSet
    }

    private File createResultsDir() {
        File projectResultsDir = (File) project.property('testResultsDir')
        File cucumberResults = new File(projectResultsDir, CUCUMBER_REPORTS_DIR)
        File sourceSetResults = new File(cucumberResults, sourceSet.name)
        sourceSetResults.mkdirs()

        return sourceSetResults
    }

    SourceSet getSourceSet() {
        return sourceSet
    }

    List<String> getTags() {
        return tags ?: extension.tags
    }

    int getMaxParallelForks() {
        return maxParallelForks ?: extension.maxParallelForks
    }

    String getStepDefinitionRoot() {
        return stepDefinitionRoot ?: extension.stepDefinitionRoot
    }

    String getFeatureRoot() {
        return featureRoot ?: extension.featureRoot
    }

    boolean getIsDryRun() {
        return isDryRun ?: extension.isDryRun
    }

    boolean getIsMonochrome() {
        return isMonochrome ?: extension.isMonochrome
    }

    boolean getIsStrict() {
        return isStrict ?: extension.isStrict
    }

    String getSnippets() {
        return snippets ?: extension.snippets
    }
}
