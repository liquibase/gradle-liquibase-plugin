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
 * This class represents the rollback-one-update-sql command.  This Liquibase Pro command doesn't
 * have a CommandStep class, so we've had to infer the arguments from the code we could find and
 * the documentation on https://docs.liquibase.com/commands/rollback/rollback-one-update-sql.html.
 *
 * @author Steven C. Saliman
 */
class RollbackOneUpdateSqlCommand extends LiquibaseCommand {

    RollbackOneUpdateSqlCommand() {
        description = 'Write SQL to rollback one update from the database to <LiquibaseOutputFile or STDOUT if not specified (Liquibase Pro key required).'
        command = 'rollback-one-update-sql'
        legacyCommand = 'rollbackOneUpdateSql'
        commandArguments = [
                CHANGELOG_FILE,
                URL_ARG,
                USERNAME,
                PASSWORD,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                DRIVER,
                DRIVER_PROPERTIES_FILE,
                DEPLOYMENT_ID,
                FORCE
        ]
    }
}
