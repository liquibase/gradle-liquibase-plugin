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
 * This class represents the rollback command.
 *
 * @author Steven C. Saliman
 */
class RollbackCommand extends LiquibaseCommand {

    RollbackCommand() {
        description = 'Rollback changes made to the database since the the <liquibaseCommandValue> tag was applied.'
        command = 'rollback'
        legacyCommand = 'rollback'
        requiresValue = true
        valueArgument = TAG
        commandArguments = [
                URL_ARG,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                USERNAME,
                PASSWORD,
                CHANGELOG_FILE,
                LABEL_FILTER,
                LABELS,
                CONTEXTS,
                ROLLBACK_SCRIPT,
                TAG,
                CHANGE_EXEC_LISTENER_CLASS,
                CHANGE_EXEC_LISTENER_PROPERTIES_FILE,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
