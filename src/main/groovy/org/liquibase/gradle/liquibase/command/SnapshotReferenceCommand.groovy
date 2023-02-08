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
 * This class represents the snapshot-reference command.
 *
 * @author Steven C. Saliman
 */
class SnapshotReferenceCommand extends LiquibaseCommand {

    SnapshotReferenceCommand() {
        description: 'Capture the current state of the reference database.'
        command = 'snapshot-reference'
        legacyCommand = 'snapshotReference'
        commandArguments = [
                REFERENCE_USERNAME,
                REFERENCE_PASSWORD,
                REFERENCE_URL,
                REFERENCE_DEFAULT_SCHEMA_NAME,
                REFERENCE_DEFAULT_CATALOG_NAME,
                SNAPSHOT_FORMAT,
                DRIVER,
                DRIVER_PROPERTIES_FILE
        ]
    }
}
