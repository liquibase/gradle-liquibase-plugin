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
 * This class represents the future-rollback-from-tag-sql command.
 *
 * @author Steven C. Saliman
 */
class FutureRollbackFromTagSqlCommand extends LiquibaseCommand {

    FutureRollbackFromTagSqlCommand() {
        description = 'Generates SQL to revert future changes up to the <liquibaseCommandValue> tag, and writes it to <liquibaseOutputFile>, or STDOUT if no file is specified.'
        command = 'future-rollback-from-tag-sql'
        legacyCommand = 'futureRollbackFromTagSql'
        requiresValue = true
        valueArgument = TAG
        commandArguments = [
                CHANGELOG_FILE,
                URL_ARG,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                USERNAME,
                PASSWORD,
                LABEL_FILTER,
                LABELS,
                CONTEXTS,
                TAG,
                DRIVER,
                DRIVER_PROPERTIES_FILE,
                OUTPUT_DEFAULT_SCHEMA,
                OUTPUT_DEFAULT_CATALOG
        ]
    }
}
