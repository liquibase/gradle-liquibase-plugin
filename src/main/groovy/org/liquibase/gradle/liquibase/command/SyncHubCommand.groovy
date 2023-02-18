/*
 * Copyright 2011-2023 Tim Berglund and Steven C. Saliman
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

package org.liquibase.gradle.liquibase.command


import org.liquibase.gradle.LiquibaseCommand

/**
 * This class represents the sync-hub command.
 *
 * @author Steven C. Saliman
 */
class SyncHubCommand extends LiquibaseCommand {

    SyncHubCommand() {
        description = 'Synchronize the local DatabaseChangeLog table with Liquibase Hub.'
        command = 'sync-hub'
        legacyCommand = 'syncHub'
        commandArguments = [
                URL_ARG,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                USERNAME,
                PASSWORD,
                CHANGELOG_FILE,
                HUB_CONNECTION_ID,
                HUB_PROJECT_ID,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
