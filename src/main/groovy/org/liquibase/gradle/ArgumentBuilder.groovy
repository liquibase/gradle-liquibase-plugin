package org.liquibase.gradle

import org.gradle.api.Project

import static org.liquibase.gradle.Util.versionAtLeast

/**
 * This class builds the Liquibase argument array for liquibase 4.4+.  Starting with Liquibase 4.4,
 * Liquibase no longer silently ignores Command Arguments that are not supported by the command, so
 * we need to be particular about how we put together the array.
 * <p>
 * This class puts together arguments in the following order:
 * <ol>
 * <li>Global Arguments</li>
 * <li>The output-file argument, if we've been given an output file.  There are two ways to specify
 * the output file.  The outputFile method in an Activity, or the liquibaseOutputFile property at
 * run time.  The runtime property wins.</li>
 * <li>Command Arguments that are allowed to come before the command, which includes most
 * arguments.</li>
 * <li>The Command itself</li>
 * <li>Command Arguments that must come after the command, such as --verbose or --exclude-objects.</li>
 * <li>Changelog parameters, which are supplied as "-D" arguments.  This only happens if we are also
 * sending the changelog-file parameter.</li>
 * <li>The command value, if we have one.  If the command has a "valueArgument" the value will be
 * sent with that argument, such as "--tag=myTag".  If not, it is simply a positional argument.
 *
 *
 * @author Steven C. Saliman
 */
class ArgumentBuilder {
    /**
     * Build the arguments, in the right order, to pass to Liquibase.
     *
     * @param project the Gradle project from which we get any "-P" parameters from the command line
     * @param activity the activity being run, which contains our global and command parameters.
     * @param liquibaseCommand the liquibase command to run.
     * @param liquibaseVersion the version of liquibase we're running
     * @return the argument string to pass to liquibase when we invoke it.
     */
    static def buildLiquibaseArgs(Project project, Activity activity, LiquibaseCommand liquibaseCommand, liquibaseVersion) {

        def liquibaseArgs = []
        def globalArgs = []
        def preCommandArgs = []
        def postCommandArgs = []

        def value = null
        def outputFile = null
        def sendingChangeLog = false

        // Special case for the dbDoc command that sets a default value for the value.  This is the
        // only command that has a default value in the plugin.
        if ( liquibaseCommand.command == "db-doc" ) {
            value = project.file("${project.buildDir}/database/docs")
        }

        // Create a merged map of activity arguments and extra arguments, then process each of the
        // arguments from the activity block, figuring out what kind of argument each one is and
        // responding accordingly.
        createArgumentMap(activity.arguments, project).each {
            def argumentName = fixArgumentName(it.key, project)
            if ( argumentName == liquibaseCommand.valueArgument ) {
                // If an activity matches the "value" argument for the command, assume the activity
                // argument is defining a default value for the command.
                value = it.value
            } else if ( LiquibaseCommand.POST_COMMAND_ARGUMENTS.contains(argumentName) ) {
                // If it is in our list of args that come after the command, add the arg to the
                // post command arguments, but only if the command supports this argument.
                if ( liquibaseCommand.commandArguments.contains(it.key) ) {
                    postCommandArgs += argumentString(argumentName, it.value)
                }
            } else if (LiquibaseCommand.COMMAND_ARGUMENTS.contains(argumentName) ) {
                // If it wasn't the value arg or a post-command arg, and it was a command arg, add
                // it to the pre-command args, but only of the command supports this argument.
                if ( liquibaseCommand.commandArguments.contains(argumentName) ) {
                    // Liquibase 4.4+ has a bug with the way it handles CLI defined changelog
                    // parameters with the drop-all command.  It fails to send them to the changelog
                    // parser, causing the parse to fail when changelogs use parameters.
                    // https://github.com/liquibase/liquibase/issues/3380  As a workaround, we won't
                    // add the argument if it is the changeLogFile arg, and we're running the
                    // drop-all command, and we're LB 44+.
                    // Ignoring case here covers both the new and legacy values
                    if ( argumentName.equalsIgnoreCase('changeLogFile') ) {
                        if ( liquibaseCommand.command == 'drop-all' && versionAtLeast(liquibaseVersion, '4.4') ) {
                            return
                        }
                        sendingChangeLog = true
                    }
                    preCommandArgs += argumentString(argumentName, it.value)
                }
            } else if ( argumentName == "outputFile" ) {
                // At this point, we're dealing with global arguments.  "outputFile" requires
                // special handling since it can be overridden at the command line.  Just save the
                // filename for later in case we didn't override it.
                outputFile = it.value
            } else {
                // If nothing matched above, then we're dealing with a global argument.
                globalArgs += argumentString(argumentName, it.value)
            }
        }

        // Look for an output file in the command line and override any output file we already have.
        if ( project.hasProperty("liquibaseOutputFile") ) {
            outputFile = project.properties.get('liquibaseOutputFile')
        }

        // Look for a value from the command line
        if ( project.hasProperty("liquibaseCommandValue") ) {
            value = project.properties.get('liquibaseCommandValue')
        }

        // Now that we've processed all the arguments, let's make sure we have a value if we needed one.
        if ( !value && liquibaseCommand.requiresValue ) {
            throw new LiquibaseConfigurationException("The Liquibase '${liquibaseCommand.command}' command requires a value")
        }

        // Now build our final argument array in the following order:
        // global args, output file (if we have one), pre command args, command, post command args,
        // changelog parameters (-D args), value
        liquibaseArgs = globalArgs

        if ( outputFile ) {
            liquibaseArgs += "--output-file=${outputFile}"
        }

        liquibaseArgs += preCommandArgs

        // We need a special case for the execute-sql-file command, which is not a real command.
        // Replace it with "execute-sql"
        if ( liquibaseCommand.command == "execute-sql-file") {
            liquibaseArgs += "execute-sql"

        } else {
            liquibaseArgs += liquibaseCommand.command
        }

        liquibaseArgs += postCommandArgs

        // If we're sending a changelog, we need to also send change log parameters.  Unfortunately,
        // due to a bug in liquibase itself (https://liquibase.jira.com/browse/CORE-2519), we need
        // to put the -D arguments after the command but before the command argument.  If we put
        // them before the command, they are ignored, and if we put them after the command value,
        // the --verbose value of the status command will not be processed correctly.
        if ( sendingChangeLog ) {
            activity.changeLogParameters.each {
                liquibaseArgs += "-D${it.key}=${it.value}"
            }
        }

        // If we have a value arg, send it as a key=value pair, otherwise, just send the value.
        if ( value ) {
            if ( liquibaseCommand.valueArgument ) {
                liquibaseArgs += argumentString(liquibaseCommand.valueArgument, value)
            } else {
                liquibaseArgs += value
            }
        }

        return liquibaseArgs

    }

    /**
     * Helper method to create an argument map out of an activity's arguments and the extra
     * arguments passed in via the {@code liquibaseExtraArguments} property.  The output of this
     * method is a map of "fixed" argument names and their values.  The extra arguments will be
     * processed after the activity arguments so that they override whatever was in the activity.
     * <p>
     * Extra arguments need to be a comma separated list of argument=value pairs.  Because of
     * limitations in Gradle, there can be no spaces in this list.
     *
     * @param arguments the arguments from the activity
     * @param project the project, from which we'll get the extra arguments.
     * @return a map of argument names and their values.
     */
    static def createArgumentMap(arguments, project) {
        def argumentMap = [:]
        // Start with the activity's arguments
        arguments.each {
            argumentMap.put(fixArgumentName(it.key, project), it.value)
        }

        // Now process the extra arguments, if we have any.
        if ( project.hasProperty("liquibaseExtraArgs") ) {
            project.liquibaseExtraArgs.split(",").each {
                def (key, value) = it.split("=")
                argumentMap.put(fixArgumentName(key, project), value)
            }
        }

        return argumentMap
    }

    /**
     * Determine the correct argument name to use when checking for supported arguments.
     * <p>
     * Some arguments that changed slightly in their names with Liquibase 4.4.  In order to support
     * the pre 4.4 names when running 4.4, we'll make the replacement, but warn users that they may
     * want to fix the activities block.
     *
     * @param argumentName the name of the argument to process
     * @param project the gradle project, used for logging
     * @return the name of the argument, as we want to send it to Liquibase.
     */
    static def fixArgumentName(argumentName, project) {
        // A map of pre 4.4 names to 4.4+ names.  Each key is the activity method we used to use,
        // and each value is what we need to use now.
        def LEGACY_TO_OPTION_MAP = [
                'changeLogFile'                 : 'changelogFile',
                'databaseChangeLogLockTableName': 'databaseChangelogLockTableName',
                'databaseChangeLogTableName'    : 'databaseChangelogTableName',
                'liquibaseHubApiKey'            : 'hubApiKey',
                'liquibaseHubUrl'               : 'hubUrl',
                'liquibaseProLicenseKey'        : 'proLicenseKey',
        ]

        def option = argumentName

        if ( LEGACY_TO_OPTION_MAP.containsKey(argumentName) ) {
            project.logger.warn("liquibase-plugin: The '${argumentName}' has been deprecated.  Please use '${LEGACY_TO_OPTION_MAP[argumentName]}' in your activity instead.")
            option = LEGACY_TO_OPTION_MAP[argumentName]
        }

        return option;
    }

    /**
     * Determine the correct argument string to send to Liquibase.  The argument name will be
     * converted to kebab-case, and we'll add the value if we have one.  If we don't we'll assume
     * we are dealing with a boolean argument.
     *
     * @param argumentName the name of the argument to process
     * @param the value of the argument to process.  If this is null, this method assumes we're
     *         dealing with a boolean argument.
     * @return the argument string to send to Liquibase.
     */
    static def argumentString(argumentName, argumentValue) {

        // convert to kebab case.
        def option = argumentName.replaceAll("([A-Z])", { Object[] it -> "-" + it[1].toLowerCase() })

        // return the right argument string.  If we don't have a value, assume a boolean argument.
        return argumentValue? "--${option}=${argumentValue}": "--${option}"
    }

}
