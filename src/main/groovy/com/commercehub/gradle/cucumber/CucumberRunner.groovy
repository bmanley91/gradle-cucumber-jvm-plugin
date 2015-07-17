package com.commercehub.gradle.cucumber

import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import net.masterthought.cucumber.ReportParser
import net.masterthought.cucumber.json.Feature
import org.gradle.api.tasks.SourceSet

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created by jgelais on 6/16/15.
 */
@Slf4j
class CucumberRunner {
    private static final String PLUGIN = '--plugin'

    CucumberRunnerOptions options
    CucumberTestResultCounter testResultCounter

    CucumberRunner(CucumberRunnerOptions options, CucumberTestResultCounter testResultCounter) {
        this.options = options
        this.testResultCounter = testResultCounter
    }

    boolean run(SourceSet sourceSet, File resultsDir, File reportsDir) {
        def features = sourceSet.resources.matching {
            options.featureRoots.each {
                include("${it}/**/*.feature")
            }
        }
        testResultCounter.beforeSuite(features.files.size())
        GParsPool.withPool(options.maxParallelForks) {
            features.files.eachParallel { File featureFile ->
                String featureName = getFeatureNameFromFile(featureFile, sourceSet)
                File resultsFile = new File(resultsDir, "${featureName}.json")
                File consoleOutLogFile = new File(resultsDir, "${featureName}-out.log")
                File consoleErrLogFile = new File(resultsDir, "${featureName}-err.log")
                File junitResultsFile = new File(resultsDir, "${featureName}.xml")

                List<String> args = []
                options.stepDefinitionRoots.each {
                    args << '--glue'
                    args << it
                }
                args << PLUGIN
                args << "json:${resultsFile.absolutePath}"
                if (options.junitReport) {
                    args << PLUGIN
                    args << "junit:${junitResultsFile.absolutePath}"
                }
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

                new JavaProcessLauncher('cucumber.api.cli.Main', sourceSet.runtimeClasspath.toList())
                        .setArgs(args)
                        .setConsoleOutLogFile(consoleOutLogFile)
                        .setConsoleErrLogFile(consoleErrLogFile)
                        .execute()
                List<CucumberFeatureResult> results = parseFeatureResult(resultsFile).collect {
                    log.debug("Logging result for $it.name")
                    createResult(it)
                }
                results.each { CucumberFeatureResult result ->
                    testResultCounter.afterFeature(result)
                }
            }
        }

        testResultCounter.afterSuite()
        return !testResultCounter.hadFailures()
    }

    String getFeatureNameFromFile(File file, SourceSet sourceSet) {
        String featureName = file.name
        sourceSet.resources.srcDirs.each { File resourceDir ->
            if (isFileChildOfDirectory(file, resourceDir)) {
                featureName = convertPathToPackage(getReleativePath(file, resourceDir))
            }
        }

        return featureName
    }

    List<Feature> parseFeatureResult(File jsonReport) {
        return new ReportParser([jsonReport.absolutePath]).features[jsonReport.absolutePath]
    }

    CucumberFeatureResult createResult(Feature feature) {
        feature.processSteps()
        CucumberFeatureResult result = new CucumberFeatureResult(
                totalScenarios: feature.numberOfScenarios,
                failedScenarios: feature.numberOfScenariosFailed,
                totalSteps: feature.numberOfSteps,
                failedSteps: feature.numberOfFailures,
                skippedSteps: feature.numberOfSkipped,
                pendingSteps: feature.numberOfPending
        )

        return result
    }

    private String convertPathToPackage(Path path) {
        return path.toString().replace(File.separator, '.')
    }

    private Path getReleativePath(File file, File dir) {
        return Paths.get(dir.toURI()).relativize(Paths.get(file.toURI()))
    }

    private boolean isFileChildOfDirectory(File file, File dir) {
        Path child = Paths.get(file.toURI())
        Path parent = Paths.get(dir.toURI())
        return child.startsWith(parent)
    }
}
