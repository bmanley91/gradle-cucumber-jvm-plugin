package com.commercehub.gradle.cucumber

/**
 * Created by jgelais on 6/16/15.
 */
interface CucumberRunnerOptions {

    List<String> getTags()

    String getStepDefinitionRoot()

    String getFeatureRoot()

    boolean getIsDryRun()

    boolean getIsMonochrome()

    boolean getIsStrict()

    String getSnippets()

    int getMaxParallelForks()

    File getBaseDir()
}
