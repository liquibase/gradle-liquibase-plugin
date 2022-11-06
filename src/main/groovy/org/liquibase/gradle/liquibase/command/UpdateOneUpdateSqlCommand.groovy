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
 * This class represents the update-one-update-sql command.  This Liquibase Pro command doesn't
 * have a CommandStep class, so we've had to infer the arguments from the code we could find and
 * the documentation on https://docs.liquibase.com/commands/update/update-one-update-sql.html.
 *
 * @author Steven C. Saliman
 */
class UpdateOneUpdateSqlCommand extends LiquibaseCommand {

    UpdateOneUpdateSqlCommand() {
        description = 'Output the SQL to deploy any specific changeset in your changelog to your database. It is only available for Liquibase Pro users'
        command = 'update-one-changeset-sql'
        legacyCommand = 'updateOneChangesetSql'
        requiresValue = true
        valueArgument = CHANGESET_ID
        commandArguments = [
                CHANGESET_ID,
                CHANGELOG_FILE,
                URL_ARG,
                USERNAME,
                PASSWORD,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
