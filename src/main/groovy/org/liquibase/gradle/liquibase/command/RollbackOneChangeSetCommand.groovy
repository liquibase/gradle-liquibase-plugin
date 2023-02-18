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
 * This class represents the rollback-one-changeset command.  This Liquibase Pro command doesn't
 * have a CommandStep class, so we've had to infer the arguments from the code we could find and
 * the documentation on https://docs.liquibase.com/commands/rollback/rollback-one-changeset.html.
 *
 * @author Steven C. Saliman
 */
class RollbackOneChangeSetCommand extends LiquibaseCommand {

    RollbackOneChangeSetCommand() {
        description = 'Roll back a specific changeset, without rolling back changesets deployed before or afterwards. (Liquibase Pro key required)'
        command = 'rollback-one-changeset'
        legacyCommand = 'rollbackOneChangeSet'
        commandArguments = [
                CHANGELOG_FILE,
                URL_ARG,
                USERNAME,
                PASSWORD,
                DEFAULT_SCHEMA_NAME,
                DEFAULT_CATALOG_NAME,
                DRIVER,
                DRIVER_PROPERTIES_FILE,
                CHANGESET_ID,
                CHANGESET_AUTHOR,
                CHANGESET_PATH,
                FORCE,
                ROLLBACK_SCRIPT
        ]
    }
}
