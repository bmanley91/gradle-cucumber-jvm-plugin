package com.commercehub.gradle.cucumber

import org.gradle.logging.ProgressLogger
import org.gradle.logging.ProgressLoggerFactory
import org.slf4j.Logger

/**
 * Created by jgelais on 6/18/15.
 */
class CucumberTestResultCounter {
    private final ProgressLoggerFactory progressLoggerFactory
    private ProgressLogger progressLogger
    private final Logger logger

    private long totalFeatures = 0
    private long completedFeatures = 0
    private long completedScenarios = 0
    private long failedScenarios = 0
    private long completedSteps = 0
    private long failedSteps = 0
    private long skippedSteps = 0
    private long pendingSteps = 0

    CucumberTestResultCounter(ProgressLoggerFactory factory, Logger logger) {
        this.progressLoggerFactory = factory
        this.logger = logger
    }

    @SuppressWarnings('SynchronizedMethod')
    public synchronized void afterFeature(CucumberFeatureResult result) {
        completedFeatures++
        completedScenarios += result.totalScenarios
        failedScenarios += result.failedScenarios
        completedSteps += result.totalSteps
        failedSteps += result.failedSteps
        skippedSteps += result.skippedSteps
        pendingSteps += result.pendingSteps
        progressLogger.progress(shortSummary)
    }

    @SuppressWarnings('DuplicateStringLiteral')
    private String getShortSummary() {
        return "$completedFeatures of ${getLabeledCount(totalFeatures, 'feature')} completed. " +
                "Scenarios [$completedScenarios completed, $failedScenarios failed]"
    }

    @SuppressWarnings('DuplicateStringLiteral')
    @SuppressWarnings('LineLength')
    private String getLongSummary() {
        return """${getLabeledCount(completedFeatures, 'feature')} completed.
    Scenarios [${formatCount(completedScenarios)} completed, ${formatCount(failedScenarios)} failed]
        Steps [${formatCount(completedSteps)} completed, ${formatCount(failedSteps)} failed, ${formatCount(skippedSteps)} skipped, ${formatCount(pendingSteps)} pending]"""
    }

    private static String formatCount(long count) {
        return count.toString().padLeft(5)
    }

    private static String getLabeledCount(long count, String noun) {
        String labeledCount = "$count $noun"
        if (count != 0) {
            labeledCount += 's'
        }

        return labeledCount
    }

    public void beforeSuite(long totalFeatures) {
        this.totalFeatures = totalFeatures
        progressLogger = progressLoggerFactory.newOperation(CucumberTestResultCounter)
        progressLogger.setDescription('Run tests')
        progressLogger.started()
        progressLogger.progress(shortSummary)
    }

    public void afterSuite() {
        progressLogger.completed()
        if (hadFailures()) {
            logger.error(longSummary)
        }
    }

    public boolean hadFailures() {
        return (failedScenarios > 0)
    }

}
