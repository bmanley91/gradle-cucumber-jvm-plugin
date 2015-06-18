package com.commercehub.gradle.cucumber

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.gradle.api.tasks.SourceSet

/**
 * Created by jgelais on 6/16/15.
 */
@Slf4j
class CucumberRunner {
    CucumberRunnerOptions options

    CucumberRunner(CucumberRunnerOptions options) {
        this.options = options
    }

    boolean run(SourceSet sourceSet, File resultsDir, File reportsDir) {
        boolean isPassing = true
        def features = sourceSet.resources.matching {
            include("${options.featureRoot}/**/*.feature")
        }
        log.info("Found ${features.files.size()} features.")
        GParsPool.withPool(options.maxParallelForks) {
            features.files.eachParallel { File featureFile ->
                File resultsFile = new File(resultsDir, "${featureFile.name}.json")
                File consoleOutLogFile = new File(resultsDir, "${featureFile.name}-out.log")
                File consoleErrLogFile = new File(resultsDir, "${featureFile.name}-err.log")

                List<String> args = []
                args << '--glue'
                args << options.stepDefinitionRoot
                args << '--plugin'
                args << "json:${resultsFile.absolutePath}"
                if (options.isDryRun) {
                    args << '--dry-run'
                }
                if (options.isMonochrome) {
                    args << '--monochrome'
                }
                if (options.isStrict) {
                    args << '--strict'
                }
                if (!options.tags.isEmpty()) {
                    args << '--tags'
                    args << options.tags.join(',')
                }
                args << '--snippets'
                args << options.snippets
                args << featureFile.absolutePath

                int exitCode =  new JavaProcessLauncher('cucumber.api.cli.Main', sourceSet.runtimeClasspath.toList())
                        .setArgs(args)
                        .setConsoleOutLogFile(consoleOutLogFile)
                        .setConsoleErrLogFile(consoleErrLogFile)
                        .execute()
                if (exitCode != 0) {
                    log.error("FAILED feature: ${featureFile.name}")
                    isPassing = false
                }
            }
        }

        return isPassing
    }
}
