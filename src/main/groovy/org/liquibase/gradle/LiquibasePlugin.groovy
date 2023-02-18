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

package org.liquibase.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.liquibase.gradle.liquibase.command.*

class LiquibasePlugin implements Plugin<Project> {

    public static final String LIQUIBASE_RUNTIME_CONFIGURATION = "liquibaseRuntime"

    void apply(Project project) {
        applyExtension(project)
        applyConfiguration(project)
        applyTasks(project)
    }


    void applyExtension(Project project) {
        def activities = project.container(Activity) { name ->
            new Activity(name)
        }
        project.configure(project) {
            extensions.create("liquibase", LiquibaseExtension, activities)
        }
    }

    void applyConfiguration(Project project) {
        project.configure(project) {
            configurations.maybeCreate(LIQUIBASE_RUNTIME_CONFIGURATION)
        }
    }

    /**
     * Create all of the liquibase tasks and add them to the project.  If the liquibaseTaskPrefix
     * is set, add that prefix to the task names.
     * @param project the project to enhance
     */
    void applyTasks(Project project) {
        // Create the tasks from an array of LiquibaseCommand objects.
        [
                new CalculateChecksumCommand(),
                new ChangelogSyncCommand(),
                new ChangelogSyncSqlCommand(),
                new ChangelogSyncToTagCommand(),
                new ChangelogSyncToTagSqlCommand(),
                new ChecksCommand(),
                new ClearChecksumsCommand(),
                new DbDocCommand(),
                new DeactivateChangelogCommand(),
                new DiffCommand(),
                new DiffChangelogCommand(),
                new DropAllCommand(),
                new ExecuteSqlCommand(),
                new ExecuteSqlFileCommand(),
                new FutureRollbackCountSqlCommand(),
                new FutureRollbackFromTagSqlCommand(),
                new FutureRollbackSqlCommand(),
                new GenerateChangelogCommand(),
                new HistoryCommand(),
                new ListLocksCommand(),
                new MarkNextChangeSetRanCommand(),
                new MarkNextChangeSetRanSqlCommand(),
                new RegisterChangelogCommand(),
                new ReleaseLocksCommand(),
                new RollbackCommand(),
                new RollbackCountCommand(),
                new RollbackCountSqlCommand(),
                new RollbackOneChangeSetCommand(),
                new RollbackOneChangeSetSqlCommand(),
                new RollbackOneUpdateCommand(),
                new RollbackOneUpdateSqlCommand(),
                new RollbackSqlCommand(),
                new RollbackToDateCommand(),
                new RollbackToDateSqlCommand(),
                new SnapshotCommand(),
                new SnapshotReferenceCommand(),
                new StatusCommand(),
                new SyncHubCommand(),
                new TagCommand(),
                new TagExistsCommand(),
                new UnexpectedChangeSetsCommand(),
                new UpdateCommand(),
                new UpdateCountCommand(),
                new UpdateCountSqlCommand(),
                new UpdateOneChangeSetCommand(),
                new UpdateOneUpdateSqlCommand(),
                new UpdateSqlCommand(),
                new UpdateTestingRollbackCommand(),
                new UpdateToTagCommand(),
                new UpdateToTagSqlCommand(),
                new ValidateCommand()
        ].each { lbCommand ->
            // The legacy command is usually just the camel case version of the command, unless
            // we've been given an override.
            if ( lbCommand.legacyCommand == null ) {
                lbCommand.legacyCommand = lbCommand.command.replaceAll("(-)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
            }

            // Match the task name to the legacy command.
            def taskName = lbCommand.legacyCommand

            // Fix the task name if we have a task prefix.
            if ( project.hasProperty('liquibaseTaskPrefix') ) {
                taskName = project.liquibaseTaskPrefix + taskName.capitalize()
            }
            project.tasks.register(taskName, LiquibaseTask) {
                group = 'Liquibase'
                description = lbCommand.description
                liquibaseCommand = lbCommand
            }
        }
    }
}
