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

package org.liquibase.gradle.liquibase.command


import org.liquibase.gradle.LiquibaseCommand

/**
 * This class represents the drop-all command.
 *
 * @author Steven C. Saliman
 */
class DropAllCommand extends LiquibaseCommand {

    DropAllCommand () {
        description = 'Drop all database objects owned by the user. Note that functions, procedures and packages are not dropped (Liquibase limitation)'
        command = 'drop-all'
        legacyCommand = 'dropAll'
        commandArguments = [
                USERNAME,
                PASSWORD,
                URL_ARG,
                DEFAULT_CATALOG_NAME,
                DEFAULT_SCHEMA_NAME,
                SCHEMAS,
                CHANGELOG_FILE,
                HUB_CONNECTION_ID,
                HUB_PROJECT_ID,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
