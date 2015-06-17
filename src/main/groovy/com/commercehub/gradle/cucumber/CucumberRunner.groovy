package com.commercehub.gradle.cucumber

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.gradle.api.internal.file.BaseDirFileResolver
import org.gradle.api.tasks.SourceSet
import org.gradle.internal.nativeintegration.services.FileSystems
import org.gradle.process.internal.DefaultJavaExecAction
import org.gradle.process.internal.JavaExecAction

/**
 * Created by jgelais on 6/16/15.
 */
@Slf4j
class CucumberRunner {
    CucumberRunnerOptions options

    CucumberRunner(CucumberRunnerOptions options) {
        this.options = options
    }

    def run(SourceSet sourceSet, File resultsDir) {
        def features = sourceSet.resources.matching {
            include('**/*.feature')
        }
        log.info("Found ${features.files.size()} features.")
        GParsPool.withPool(options.maxParallelForks) {
            features.files.eachParallel { File featureFile ->
                File resultsFile = new File(resultsDir, "${featureFile.name}.json")

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

                JavaExecAction runner = new DefaultJavaExecAction(new BaseDirFileResolver(FileSystems.getDefault(), options.baseDir))
                runner.setMain('cucumber.api.cli.Main')
                runner.setClasspath(sourceSet.runtimeClasspath)
                runner.setArgs(args)
                runner.execute()
            }
        }
    }
}
