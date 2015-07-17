package com.commercehub.gradle.cucumber

/**
 * Created by jgelais on 6/16/15.
 */
interface CucumberRunnerOptions {

    List<String> getTags()

    List<String> getStepDefinitionRoots()

    List<String> getFeatureRoots()

    boolean getIsDryRun()

    boolean getIsMonochrome()

    boolean getIsStrict()

    String getSnippets()

    int getMaxParallelForks()

    boolean getJunitReport()
}
