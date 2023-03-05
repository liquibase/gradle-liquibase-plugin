package org.liquibase.gradle

import org.gradle.api.Project

/**
 * This class builds arguments that can be passed into Liquibase versions prior to 4.4.
 * <p>
 * The legacy argument builder is pretty simple.  All the arguments we get from an activity are
 * generally passed as-is to Liquibase, and we always use the legacy command from the given command.
 *
 * @author Steven C. Saliman
 */
class LegacyArgumentBuilder {
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
        def args = []

        // liquibase forces to add command params after the the command.  We This list is based off
        // of the Liquibase code, and reflects the things That liquibase will explicitly look for in
        // the command params.  Note That when liquibase process a command param, it still sets the
        // appropriate Main class instance variable, so we don't need to worry too much about
        // mapping params to commands.
        def postCommandArgs = [
                'excludeObjects',
                'includeObjects',
                'schemas',
                'snapshotFormat',
                'sql',
                'sqlFile',
                'delimiter',
                'rollbackScript',
                'outputFile'
        ]

        def sendingChangeLog = false

        // Create a merged map of activity args and extra args.
        def argumentMap = createArgumentMap(activity.arguments, project)

        // Start by printing a warning about any activity arguments that will change in Liquibase
        // 4.4 so that users have some warning before they upgrade Liquibase.
        printDeprecationWarnings(argumentMap, project)

        // Start with the global params (the ones not in our post command argument list)
        argumentMap.findAll({ !postCommandArgs.contains(it.key) }).each {
            if ( it.key.equalsIgnoreCase('changeLogFile') ) {
                sendingChangeLog = true
            }
            args += "--${it.key}=${it.value}"
        }

        // Add the output file, which believe it or not is a global argument.  The command line
        // property wins over the activity argument.
        if ( project.properties.get("liquibaseOutputFile") ) {
            args += "--outputFile=${project.properties.get('liquibaseOutputFile')}"
        } else if ( argumentMap.outputFile ) {
            args += "--outputFile=${argumentMap.outputFile}"
        }

        // the "executeSqlFile" command needs some special attention, since it is not a real
        // command.  It needs to be replaced with "executeSql"
        if ( liquibaseCommand.legacyCommand == "executeSqlFile") {
            args += "executeSql"
        } else {
            args += liquibaseCommand.legacyCommand
        }

        // Add the post-command arguments after the command.
        argumentMap.findAll({ postCommandArgs.contains(it.key) }).each {
            // skip the output file, we've already added that
            if ( it.key.equalsIgnoreCase('outputFile') ) {
                return
            }
            args += "--${it.key}=${it.value}"
        }


        def value = project.properties.get("liquibaseCommandValue")

        // Special case for the dbDoc command.  This is the only command that has a default value
        // in the plugin.
        if ( !value && liquibaseCommand.command == "db-doc" ) {
            value = project.file("${project.buildDir}/database/docs")
        }

        if ( !value && liquibaseCommand.requiresValue ) {
            throw new LiquibaseConfigurationException("The Liquibase '${liquibaseCommand.command}' command requires a value")
        }

        // If we're sending a changelog, we need to also send change log parameters.  Unfortunately,
        // due to a bug in liquibase itself (https://liquibase.jira.com/browse/CORE-2519), we need
        // to put the -D arguments after the command but before the command argument.  If we put
        // them before the command, they are ignored, and if we put them after the command value,
        // the --verbose value of the status command will not be processed correctly.
        if ( sendingChangeLog ) {
            activity.changeLogParameters.each {
                args += "-D${it.key}=${it.value}"
            }
        }

        // Because of Liquibase CORE-2519, a verbose status only works when --verbose is placed
        // last.  Fortunately, this doesn't break the other commands, who appear to be able to
        // handle -D options between the command and the value.
        if ( value ) {
            args += value
        }

        return args
    }

    /**
     * Helper method to go through all the activity's arguments and print deprecation warnings if
     * any of them are destined to go away in Liquibase 4.4.
     *
     * @param activity the activity with the arguments to check
     * @param project the gradle project, used for logging
     */
    static def printDeprecationWarnings(arguments, project) {
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
        arguments.each {
            def argumentName = it.key
            if ( LEGACY_TO_OPTION_MAP.containsKey(argumentName) ) {
                project.logger.warn("liquibase-plugin: The '${argumentName}' has been deprecated in Liquibase 4.4, and will need to be replaced with '${LEGACY_TO_OPTION_MAP[argumentName]}' in your activity.")
            }
        }
    }

    /**
     * Helper method to create an argument map out of an activity's arguments and the extra
     * arguments passed in via the {@code liquibaseExtraArguments} property.  The output of this
     * method is a map of argument names and their values.  The extra arguments will be processed
     * after the activity arguments so that they override whatever was in the activity.
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
            argumentMap.put(it.key, it.value)
        }

        // Now process the extra arguments, if we have any.
        if ( project.hasProperty("liquibaseExtraArgs") ) {
            project.liquibaseExtraArgs.split(",").each {
                def (key, value) = it.split("=")
                argumentMap.put(key, value)
            }
        }

        return argumentMap
    }
}
