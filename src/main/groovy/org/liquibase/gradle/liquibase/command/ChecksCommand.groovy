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
 * This class represents the checks command.  This command requires a value, but unlike most
 * commands that require a value, this command doesn't have a {@code valueArgument}.  We just send
 * the "liquibaseCommandValue" value as a positional value at the end of the command.
 *
 * @author Steven C. Saliman
 */
class ChecksCommand extends LiquibaseCommand {

    ChecksCommand() {
        description =  'Execute the <liquibaseCommandValue> quality check'
        command =  'checks'
        legacyCommand =  'checks'
        requiresValue =  true
        commandArguments =  [
                AUTO_UPDATE,
                CHECKS_SETTINGS_FILE,
                CHECK_NAME,
                CHANGELOG_FILE,
                FORMAT
        ]
    }
}
