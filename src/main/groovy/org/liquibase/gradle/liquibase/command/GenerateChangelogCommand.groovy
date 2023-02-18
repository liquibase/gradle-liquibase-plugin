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
 * This class represents the generate-changelog command.  Unlike most commands that take database
 * arguments, this command doesn't take default-schema-name or default-catalog-name.
 *
 * @author Steven C. Saliman
 */
class GenerateChangelogCommand extends LiquibaseCommand {

    GenerateChangelogCommand() {
        description =  'Generate a Groovv changelog from the current state of the database to <LiquibaseOutputFile> or STDOUT of no file is specified.'
        command =  'generate-changelog'
        legacyCommand =  'generateChangelog'
        commandArguments = [
                USERNAME,
                PASSWORD,
                URL_ARG,
                CHANGELOG_FILE,
                DATA_OUTPUT_DIRECTORY,
                EXCLUDE_OBJECTS,
                INCLUDE_OBJECTS,
                INCLUDE_SCHEMA,
                INCLUDE_CATALOG,
                INCLUDE_TABLESPACE,
                SCHEMAS,
                DIFF_TYPES,
                DRIVER,
                DRIVER_PROPERTIES_FILE,
                OVERWRITE_OUTPUT_FILE,
        ]
    }
}
