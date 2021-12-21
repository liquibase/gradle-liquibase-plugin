/*
 * Copyright 2011-2021 Tim Berglund and Steven C. Saliman
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

import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction

/**
 * Gradle task that calls Liquibase to run a command.
 *
 * @author Stven C. Saliman
 */
class LiquibaseTask extends JavaExec {

	/**
	 * The Liquibase command to run.
	 */
	@Input
	def command
	/**
	 * Whether or not the command needs a value, such as "tag" or "rollbackCount"
	 */
	@Input
	def requiresValue = false

	@TaskAction
	@Override
	public void exec() {

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
		def args = []
		// liquibase forces to add command params after the the command.  We This list is based off
        // of the Liquibase code, and reflects the things That liquibase will explicitly look for in
        // the command params.  Note That when liquibase process a command param, it still sets the
		// appropriate Main class instance variable, so we don't need to worry too much about
        // mapping params to commands.
		def commandParams = [
			'excludeObjects',
			'includeObjects',
			'schemas',
			'snapshotFormat',
			'sql',
			'sqlFile',
			'delimiter',
			'rollbackScript'
		]
		activity.arguments.findAll( { !commandParams.contains(it.key) } ) .each {
			args += "--${it.key}=${it.value}"
		}

		if ( command ) {
			args += command
		}
		// Add the command parameters after the command.
		activity.arguments.findAll( { commandParams.contains(it.key) } ) .each {
			args += "--${it.key}=${it.value}"
		}


		def value = project.properties.get("liquibaseCommandValue")

		// Special case for the dbDoc command.  This is the only command that has a default value
        // in the plugin.
		if ( !value && command == "dbDoc" ) {
			value = project.file("${project.buildDir}/database/docs")
		}

		if ( !value && requiresValue ) {
			throw new LiquibaseConfigurationException("The Liquibase '${command}' command requires a value")
		}

		// Unfortunately, due to a bug in liquibase itself
		// (https://liquibase.jira.com/browse/CORE-2519), we need to put the -D arguments after the
        // command but before the command argument.  If we put them before the command, they are
        // ignored, and if we put them after the command value, the --verbose value of the status
		// command will not be processed correctly.
		activity.parameters.each {
			args += "-D${it.key}=${it.value}"
		}

		// Because of Liquibase CORE-2519, a verbose status only works when --verbose is placed
        // last.  Fortunately, this doesn't break the other commands, who appear to be able to
        // handle -D options between the command and the value.
		if ( value ) {
			args += value
		}

		// Set values on the JavaExec task
		setArgs(args)

		def classpath = project.configurations.getByName(LiquibasePlugin.LIQUIBASE_RUNTIME_CONFIGURATION)
		if ( classpath == null || classpath.isEmpty() ) {
			throw new LiquibaseConfigurationException("No liquibaseRuntime dependencies were defined.  You must at least add Liquibase itself as a liquibaseRuntime dependency.")
		}
		setClasspath(classpath)
		fixMainClass()
		// "inherit" the system properties from the Gradle JVM.
		systemProperties System.properties
		println "liquibase-plugin: Running the '${activity.name}' activity..."
		project.logger.debug("liquibase-plugin: The ${getMain()} class will be used to run Liquibase")
		project.logger.debug("liquibase-plugin: Liquibase will be run with the following jvmArgs: ${project.liquibase.jvmArgs}")
		setJvmArgs(project.liquibase.jvmArgs)
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
	 * This method detects the resolved version of Liquibase in the liquibaseRuntime configuration
     * and chooses the right default based on the version it finds.
	 * <p>
	 * If for some reason, it finds Liquibase in the classpath more than once, the last one it
     * finds, wins.
	 */
	def fixMainClass() {
		if (project.extensions.findByType(LiquibaseExtension.class).mainClassName ) {
			project.logger.debug("liquibase-plugin: The extension's mainClassName was set, skipping version detection.")
			return
		}

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

		if ( lbAtLeast(foundVersion, '4.4') ) {
			project.extensions.findByType(LiquibaseExtension.class).mainClassName = 'liquibase.integration.commandline.LiquibaseCommandLine'
			project.logger.debug("liquibase-plugin: Using the 4.4+ command line parser.")

		} else {
			project.extensions.findByType(LiquibaseExtension.class).mainClassName = 'liquibase.integration.commandline.Main'
			project.logger.debug("liquibase-plugin: Using the pre 4.4 command line parser.")
		}

	}

	/**
	 * Compare a given Liquibase semver to a target semver and return whether the given semver is
     * at least the version of the target.
     *
	 * @param givenSemver the version of Liquibase found in the classpath
	 * @param targetSemver the target version to use as a comparison.
	 * @return @{code true} if the given version is greater than or equal to the target semver.
	 */
	def lbAtLeast(givenSemver, targetSemver) {
		List givenVersions = givenSemver.tokenize('.')
		List targetVersions = targetSemver.tokenize('.')

		def commonIndices = Math.min(givenVersions.size(), targetVersions.size())

		for (int i = 0; i < commonIndices; ++i) {
			def givenNum = givenVersions[i].toInteger()
			def targetNum = targetVersions[i].toInteger()

			if (givenNum != targetNum) {
				return givenNum > targetNum
			}
		}

		// If we got this far then all the common indices are identical, so whichever version is
        // longer must be more recent
		return givenVersions.size() > targetVersions.size()
	}
}
