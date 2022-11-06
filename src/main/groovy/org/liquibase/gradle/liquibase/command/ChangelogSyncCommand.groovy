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
 * This class represents the changelog-sync command.
 *
 * @author Steven C. Saliman
 */
class ChangelogSyncCommand extends LiquibaseCommand {

    ChangelogSyncCommand() {
        description =  'Mark all changes as executed in the database.'
        command =  'changelog-sync'
        legacyCommand = 'changelogSync'
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
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
