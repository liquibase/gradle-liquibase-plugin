/*
 * Copyright 2011-2021 Tim Berglund and Steven C. Saliman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.liquibase.gradle

/**
 * Liquibase 3.6 changed the way logging is done, but broke console output when
 * running from the Gradle Plugin (JIRA CORE-3220).  There is a pull request in
 * place (#770), but until it is merged in, console output won't work normally.
 *
 * This class uses a really ugly reflection hack to enable console output before
 * running Liquibase.
 *
 * Note that this won't fix the fact that the --logLevel option no longer works,
 * but it will at least let users see INFO logging.
 *
 * @author Steven C. Saliman
 */
class OutputEnablingLiquibaseRunner {
	// The name of the Main class in Liquibase itself.
	def static MAIN_CLASS = 'liquibase.integration.commandline.Main'

	static void main(String[] args) {

		def main = Class.forName MAIN_CLASS

		// Get the consoleLogFilter field of the main class.  This version
		// throw exception if not found.
		def consoleLogFilterField = main.declaredFields.find {
			it.name == 'consoleLogFilter'
		}

		// If we found a consoleLogFilter, it's value is an instance of an
		// inner class of Main.  Get the outputLogs field from that instance
		// and set it to true.
		if ( consoleLogFilterField ) {
			def outputLogsField
			def consoleLogFilterInstance
			consoleLogFilterField.setAccessible(true)
			consoleLogFilterInstance = consoleLogFilterField.get(null)
			outputLogsField = consoleLogFilterInstance.class.declaredFields.find {
				it.name == 'outputLogs'
			}
			if ( outputLogsField ) {
				outputLogsField.setAccessible(true)
				outputLogsField.set(consoleLogFilterInstance, true)
			}
		}

		// Now we can run Liquibase.
		main.main args
	}
}
