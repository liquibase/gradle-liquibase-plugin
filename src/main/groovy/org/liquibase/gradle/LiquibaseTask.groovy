/*
 * Copyright 2011-2022 Tim Berglund and Steven C. Saliman
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing permissions and limitations
 * under the License.
 */

package org.liquibase.gradle

import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

import static org.liquibase.gradle.Util.versionAtLeast

/**
 * Gradle task that calls Liquibase to run a command.
 *
 * @author Stven C. Saliman
 */
class LiquibaseTask extends JavaExec {

    /**
     * The Liquibase command to run
     */
    @Input
    LiquibaseCommand liquibaseCommand

    @TaskAction
    @Override
    void exec() {

        def activities = project.liquibase.activities
        def runList = project.liquibase.runList

        if ( activities == null || activities.size() == 0 ) {
            throw new LiquibaseConfigurationException("No activities defined.  Did you forget to add a 'liquibase' block to your build.gradle file?")
        }

        if ( runList != null && runList.trim().size() > 0 ) {
            runList.split(',').each { activityName ->
                activityName = activityName.trim()
                def activity = activities.find { it.name == activityName }
                if ( activity == null ) {
                    throw new LiquibaseConfigurationException("No activity named '${activityName}' is defined the liquibase configuration")
                }
                runLiquibase(activity)
            }
        } else
            activities.each { activity ->
                runLiquibase(activity)
            }
    }

    /**
     * Build the proper command line and call Liquibase.
     *
     * @param activity the activity holding the Liquibase particulars.
     */
    def runLiquibase(activity) {
        def liquibaseVersion = findLiquibaseVersion()

        // Set values on the JavaExec task using the Argument Builder appropriate for the Liquibase
        // version we have.
        if ( versionAtLeast(liquibaseVersion, '4.4') ) {
            setArgs(ArgumentBuilder.buildLiquibaseArgs(project, activity, liquibaseCommand, liquibaseVersion))
        } else {
            logger.warn("using legacy argument builder.  Consider updating to Liquibase 4.4+")
            setArgs(LegacyArgumentBuilder.buildLiquibaseArgs(project, activity, liquibaseCommand, liquibaseVersion))
        }

        def classpath = project.configurations.getByName(LiquibasePlugin.LIQUIBASE_RUNTIME_CONFIGURATION)
        if ( classpath == null || classpath.isEmpty() ) {
            throw new LiquibaseConfigurationException("No liquibaseRuntime dependencies were defined.  You must at least add Liquibase itself as a liquibaseRuntime dependency.")
        }
        setClasspath(classpath)
        fixMainClass(liquibaseVersion)
        // "inherit" the system properties from the Gradle JVM.
        systemProperties System.properties
        println "liquibase-plugin: Running the '${activity.name}' activity..."
        project.logger.debug("liquibase-plugin: The ${getMain()} class will be used to run Liquibase")
        project.logger.debug("liquibase-plugin: Liquibase will be run with the following jvmArgs: ${project.liquibase.jvmArgs}")
        jvmArgs(project.liquibase.jvmArgs)
        project.logger.debug("liquibase-plugin: Running 'liquibase ${args.join(" ")}'")
        super.exec()
    }

    /**
     * Watch for changes to the extension's mainClassName and make sure the task's main class is
     * set correctly.  This method was created because Gradle 6.4 made changes to the main class
     * preventing us from calling setMain during the execution phase.
     *
     * @param closure
     * @return
     */
    @Override
    Task configure(Closure closure) {
        conventionMapping("main") {
            project.extensions.findByType(LiquibaseExtension.class).mainClassName
        }
        return super.configure(closure)
    }

    /**
     * Fix the main class to be used when running Liquibase.  Since we can't call setMain directly
     * in Gradle 6.4+, we had to register a listener that watched for changes to the extension's
     * "mainClassName" property.  But if the user didn't set a value, we'll need to set one before
     * we try to run Liquibase so the property listener can set the class name correctly.
     * <p>
     * This method chooses the right default based on the version of liquibase it is given.
     *
     * @param liquibaseVersion the version of liquibase we're using.
     */
    def fixMainClass(liquibaseVersion) {
        if ( project.extensions.findByType(LiquibaseExtension.class).mainClassName ) {
            project.logger.debug("liquibase-plugin: The extension's mainClassName was set, skipping version detection.")
            return
        }

        if ( versionAtLeast(liquibaseVersion, '4.4') ) {
            project.extensions.findByType(LiquibaseExtension.class).mainClassName = 'liquibase.integration.commandline.LiquibaseCommandLine'
            project.logger.debug("liquibase-plugin: Using the 4.4+ command line parser.")

        } else {
            project.extensions.findByType(LiquibaseExtension.class).mainClassName = 'liquibase.integration.commandline.Main'
            project.logger.warn("liquibase-plugin: Using the pre 4.4 command line parser.  Consider updating to Liquibase 4.4+")
        }

    }

    /**
     * This method detects the resolved version of Liquibase in the liquibaseRuntime configuration
     * <p>
     * If for some reason, it finds Liquibase in the classpath more than once, the last one it
     * finds, wins.
     *
     * @param project the Gradle project object from which we can get a classpath.
     * @return the version of Liquibase found
     * @throws LiquibaseConfigurationException if no version if Liquibase is found at runtime.
     */
    def findLiquibaseVersion() {
        def foundVersion
        def config = project.configurations.liquibaseRuntime
        config.resolvedConfiguration.resolvedArtifacts.each { dep ->
            def moduleName = dep.moduleVersion.id.name
            def moduleVersion = dep.moduleVersion.id.version
            if ( moduleName == 'liquibase-core' ) {
                project.logger.debug("liquibase-plugin: Found version ${moduleVersion} of liquibase-core.")
                if ( foundVersion && foundVersion != moduleVersion ) {
                    project.logger.warn("liquibase-plugin: More than one version of the liquibase-core dependency was found in the liquibaseRuntime configuration!")
                }
                foundVersion = moduleVersion
            }
        }
        if ( !foundVersion ) {
            throw new LiquibaseConfigurationException("Liquibase-core was not found  not found in the liquibaseRuntime configuration!")
        }
        return foundVersion
    }

}
