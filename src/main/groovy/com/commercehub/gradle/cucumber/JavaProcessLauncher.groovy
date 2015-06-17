package com.commercehub.gradle.cucumber

import groovy.util.logging.Slf4j
import org.zeroturnaround.exec.ProcessExecutor

/**
 * Created by jgelais on 6/17/15.
 */
@Slf4j
class JavaProcessLauncher {
    String mainClassName
    List<File> classpath
    List<String> args = []
    File consoleOutLogFile
    File consoleErrLogFile

    JavaProcessLauncher(String mainClassName, List<File> classpath) {
        this.mainClassName = mainClassName
        this.classpath = classpath
    }

    JavaProcessLauncher setArgs(List<String> args) {
        this.args = args*.toString()
        return this
    }

    JavaProcessLauncher setConsoleOutLogFile(File consoleOutLogFile) {
        this.consoleOutLogFile = consoleOutLogFile
        return this
    }

    JavaProcessLauncher setConsoleErrLogFile(File consoleErrLogFile) {
        this.consoleErrLogFile = consoleErrLogFile
        return this
    }

    int execute() {
        List<String> command = []
        command << javaCommand
        command << '-cp'
        command << classPathAsString
        command << mainClassName
        command.addAll(args)

        ProcessExecutor processExecutor = new ProcessExecutor().command(command)
        if (consoleOutLogFile) {
            processExecutor.redirectOutput(consoleOutLogFile.newOutputStream())
        }
        if (consoleErrLogFile) {
            processExecutor.redirectError(consoleErrLogFile.newDataOutputStream())
        }
        log.debug("Running command [${command.join(' ')}]")
        return processExecutor.execute().exitValue
    }

    String getClassPathAsString() {
        return classpath*.absolutePath.join(System.getProperty('path.separator'))
    }

    static String getJavaCommand() {
        File javaHome = new File(System.getProperty('java.home'))
        return new File(new File(javaHome, 'bin'), javaExecutable).absolutePath
    }

    static String getJavaExecutable() {
        return System.getProperty('os.name').contains('win') ? 'java.exe' : 'java'
    }
}
