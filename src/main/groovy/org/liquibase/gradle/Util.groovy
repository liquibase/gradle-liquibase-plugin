package org.liquibase.gradle

import liquibase.command.CommandArgumentDefinition
import liquibase.command.CommandDefinition

/**
 * Utility class to hold our helper methods.
 *
 * @author Steven C. Saliman
 */
class Util {
    /**
     * Compare a given semver to a target semver and return whether the given semver is at least the
     * version of the target.
     *
     * @param givenSemver the version of Liquibase found in the classpath
     * @param targetSemver the target version to use as a comparison.
     * @return @{code true} if the given version is greater than or equal to the target semver.
     */
    static def versionAtLeast(String givenSemver, String targetSemver) {
        List givenVersions = givenSemver.tokenize('.')
        List targetVersions = targetSemver.tokenize('.')

        def commonIndices = Math.min(givenVersions.size(), targetVersions.size())

        for ( int i = 0; i < commonIndices; ++i ) {
            def givenNum = givenVersions[i].toInteger()
            def targetNum = targetVersions[i].toInteger()

            if ( givenNum != targetNum ) {
                return givenNum > targetNum
            }
        }

        // If we got this far then all the common indices are identical, so whichever version is
        // longer must be more recent
        return givenVersions.size() > targetVersions.size()
    }

    /**
     * Get the command arguments for a Liquibase command
     * @param liquibaseCommand the Liquibase CommandDefinition whose arguments we need.
     * @return an array of supported arguments.
     */
    static def argumentsForCommand(CommandDefinition liquibaseCommand) {
        // Build a list of all the arguments (and argument aliases) supported by the given command.
        def supportedCommandArguments = []
        liquibaseCommand.getArguments().each { argName, a ->
            supportedCommandArguments += a.name
            // Starting with Liquibase 4.16, command arguments can have aliases
            def supportsAliases = CommandArgumentDefinition.getDeclaredFields().find { it.name == "aliases" }
            if ( supportsAliases ) {
                supportedCommandArguments += a.aliases
            }
        }
        return supportedCommandArguments

    }

}
