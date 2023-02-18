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
 * This class represents the execute-sql command when running with a file argument.  This command
 * Doesn't really exist in Liquibase, but is a bit of syntactic sugar to make running SQL files
 * easier to deal with.  It runs the "execute-sql" command, but assumes the value argument is
 * "sql-file".
 *
 * @author Steven C. Saliman
 */
class ExecuteSqlFileCommand extends LiquibaseCommand {

    ExecuteSqlFileCommand() {
        description =  'Execute a SQL file given in <liquibaseCommandValue>"'
        // These are not the arguments that will be executed, but we need unique command to create
        // our tasks correctly.  Argument builders will need to treat this as a special case.
        command = 'execute-sql-file'
        legacyCommand = 'executeSqlFile'
        requiresValue = true
        valueArgument = SQL_FILE
        commandArguments = [
                URL_ARG,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                USERNAME,
                PASSWORD,
                SQL,
                SQL_FILE,
                DELIMITER,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
