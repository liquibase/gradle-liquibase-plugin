package org.liquibase.gradle

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
    static def versionAtLeast(givenSemver, targetSemver) {
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

}
