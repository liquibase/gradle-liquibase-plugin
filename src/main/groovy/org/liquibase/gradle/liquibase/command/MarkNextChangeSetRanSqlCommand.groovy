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
 * This class represents the mark-next-changeset-ran-sql command.
 *
 * @author Steven C. Saliman
 */
class MarkNextChangeSetRanSqlCommand extends LiquibaseCommand {


    MarkNextChangeSetRanSqlCommand() {
        description = 'Write SQL to mark the next change you apply as executed in the database to <liquibaseOutputFile>, or STDOUT if no value is provided.'
        command = 'mark-next-changeset-ran-sql'
        legacyCommand = 'markNextChangeSetRanSql'
        commandArguments = [
                URL_ARG,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                USERNAME,
                PASSWORD,
                CHANGELOG_FILE,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
