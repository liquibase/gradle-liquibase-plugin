package org.liquibase.gradle

import org.gradle.api.GradleException

/**
 * Custom Gradle exception thrown by the plugin when the plugin hasn't been
 * configured correctly.
 *
 * @author Steven C. Saliman
 */
class LiquibaseConfigurationException extends GradleException {
	LiquibaseConfigurationException(String msg) {
		super(msg)
	}

}
