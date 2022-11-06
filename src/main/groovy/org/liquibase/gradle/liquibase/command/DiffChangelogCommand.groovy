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
 * This class represents the diff-changelog command.
 *
 * @author Steven C. Saliman
 */
class DiffChangelogCommand extends LiquibaseCommand {

    DiffChangelogCommand() {
        description = 'Compare two databases to produce changesets and write them to a changelog file'
        command = 'diff-changelog'
        legacyCommand = 'diffChangeLog'
        commandArguments = [
                REFERENCE_USERNAME,
                REFERENCE_PASSWORD,
                REFERENCE_URL,
                REFERENCE_DEFAULT_CATALOG_NAME,
                REFERENCE_DEFAULT_SCHEMA_NAME,
                USERNAME,
                PASSWORD,
                URL_ARG,
                DEFAULT_CATALOG_NAME,
                DEFAULT_SCHEMA_NAME,
                CHANGELOG_FILE,
                EXCLUDE_OBJECTS,
                INCLUDE_OBJECTS,
                INCLUDE_TABLESPACE,
                SCHEMAS,
                INCLUDE_SCHEMA,
                INCLUDE_CATALOG,
                DIFF_TYPES,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }

}
