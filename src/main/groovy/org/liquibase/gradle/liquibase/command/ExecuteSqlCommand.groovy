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
 * This class represents the execute-sql command.
 *
 * @author Steven C. Saliman
 */
class ExecuteSqlCommand extends LiquibaseCommand {

    ExecuteSqlCommand() {
        description =  'Execute a SQL string or file given in <liquibaseCommandValue> in this format: -PliquibaseCommandValue="--sql=select 1" or -PliquibaseCommandValue="--sql-file=myfile.sql"'
        command = 'execute-sql'
        legacyCommand = 'executeSql'
        // TODO: This needs a little more work.  Maybe require no arg, but put it in extraArgs?
        requiresValue = true // No valueParam here because we support two, and it needs to be in the value itself.
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
