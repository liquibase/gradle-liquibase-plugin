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

        // Start with the global params (the ones not in our post command argument list)
        activity.arguments.findAll({ !postCommandArgs.contains(it.key) }).each {
            if ( it.key.equalsIgnoreCase('changeLogFile') ) {
                sendingChangeLog = true
            }
            args += "--${it.key}=${it.value}"
        }

        // Add the output file, which believe it or not is a global argument.  The command line
        // property wins over the activity argument.
        if ( project.properties.get("liquibaseOutputFile") ) {
            args += "--outputFile=${project.properties.get('liquibaseOutputFile')}"
        } else if ( activity.arguments.outputFile ) {
            args += "--outputFile=${activity.arguments.outputFile}"
        }

        // the "executeSqlFile" command needs some special attention, since it is not a real
        // command.  It needs to be replaced with "executeSql"
        if ( liquibaseCommand.legacyCommand == "executeSqlFile") {
            args += "executeSql"
        } else {
            args += liquibaseCommand.legacyCommand
        }

        // Add the post-command arguments after the command.
        activity.arguments.findAll({ postCommandArgs.contains(it.key) }).each {
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

}
