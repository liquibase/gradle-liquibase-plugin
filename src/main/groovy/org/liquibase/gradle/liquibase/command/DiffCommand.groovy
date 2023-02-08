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
 * This class represents the diff command.  Unlike most commands that support Database arguments,
 * this command doesn't appear to support the default-schema or default-catalog arguments.
 *
 * @author Steven C. Saliman
 */
class DiffCommand extends LiquibaseCommand {

    DiffCommand() {
        description = 'Compare two databases and write differences to <liquibaseOutputFile> or STDOUT of no file is specified.'
        command = 'diff'
        legacyCommand = 'diff'
        commandArguments = [
                REFERENCE_USERNAME,
                REFERENCE_PASSWORD,
                REFERENCE_URL,
                USERNAME,
                PASSWORD,
                URL_ARG,
                EXCLUDE_OBJECTS,
                INCLUDE_OBJECTS,
                SCHEMAS,
                DIFF_TYPES,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
