package com.commercehub.gradle.cucumber

import org.gradle.api.Project

/**
 * Created by jgelais on 6/11/15.
 */
class CucumberExtension {
    /**
     * Tags used to filter which scenarios should be run.
     *
     * Defaults to an empty list.
     */
    List<String> tags = []

    /**
     * Maximum number of parallel threads to run features on.
     *
     * Defaults to 1.
     */
    int maxParallelForks = 1

    /**
     *
     */
    List<String> stepDefinitionRoots = ['cucumber.steps', 'cucumber.hooks']

    /**
     *
     */
    List<String> featureRoots = ['features']

    /**
     *
     */
    boolean isDryRun = false

    /**
     *
     */
    boolean isMonochrome = false

    /**
     *
     */
    boolean isStrict = false

    /**
     *
     */
    String snippets = 'camelcase'

    /**
     * Property to enable/disable junit reporting
     */
    boolean junitReport = false

    private final Project project

    CucumberExtension(Project project) {
        this.project = project
    }

    def cucumber(Closure closure) {
        closure.setDelegate this
        closure.call()
    }

    void setMaxParallelForks(int maxParallelForks) {
        if (maxParallelForks < 1) {
            throw new IllegalArgumentException('maxParallelForks most be a positive integer. ' +
                    "You supplied: $maxParallelForks")
        }
        this.maxParallelForks = maxParallelForks
    }

    @SuppressWarnings('DuplicateStringLiteral')
    void setSnippets(String snippets) {
        if (!['camelcase', 'underscore', null].contains(snippets)) {
            throw new IllegalArgumentException('Legal values for snippets include [camelcase, underscore]. ' +
                    "You provided: ${snippets}")
        }
        this.snippets = snippets
    }
}
