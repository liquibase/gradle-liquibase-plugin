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
        // Create the tasks from an array of maps containing task definition metadata.  To keep
        // things compact, we use really short key names:
        // c: The Liquibase 4.4+ name of the command.  The name of the task will be the camel case
        //    equivalent of the command.
        // rv: Does the command require a value?
        // l: In some cases, the legacy name of the command is not the same as the camel case
        //    version of the command.  For example, the legacy name of calculate-checksum is
        //    legacyCheckSum with a capital S.
        // d: A description for the command.
        [
                [c:'calculate-checksum', rv:true, l:'calculateCheckSum', d:'Calculate and print a checksum for the <liquibaseCommandValue> changeset.'],
                [c:'changelog-sync', rv:false, d: 'Mark all changes as executed in the database.'],
                [c:'changelog-sync-sql', rv:false, d:'Output the raw SQL used by Liquibase when running changelogSync.'],
                [c:'changelog-sync-to-tag', rv:true, d:'Mark all undeployed changesets up to and including the <liquibaseCommandValue> tag as executed in your database.'],
                [c:'changelog-sync-to-tag-sql', rv:true, d:'Output the raw SQL used by Liquibase when running the changelogSyncToTag command.'],
                [c:'checks', rv:true, d:'Execute the <liquibaseCommandValue> quality check'],
                [c:'clear-checksums', rv:false, d:'Remove all saved checksums from the database. On next run checksums will be recomputed.  Useful for "MD5Sum Check Failed" errors.'],
                [c:'db-doc', rv:true, d:'Generates Javadoc-like documentation for the existing database and changelogs to the <liquibaseCommandValue> directory.'],
                [c:'deactivate-changelog', rv:false, n:'deactivateChangeLog', d:'Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub.'],
                [c:'diff', rv:false, d:'Compare two databases and write differences to STDOUT.'],
                [c:'diff-changelog', rv:false, l:'diffChangeLog', d:'Compare two databases to produce changesets and write them to a changelog file'],
                [c:'drop-all', rv:false, d:'Drop all database objects owned by the user. Note that functions, procedures and packages are not dropped (Liquibase limitation)'],
                [c:'execute-sql', rv:true, d:'Execute a SQL string or file given in <liquibaseCommandValue> in this format: -PliquibaseCommandValue="--sql=select 1" or -PliquibaseCommandValue="--sqlFile=myfile.sql"'],
                [c:'future-rollback-count-sql', rv:true, d:'Generates SQL to sequentially revert <liquibaseCommandValue> changes the database.'],
                [c:'future-rollback-from-tag-sql', rv:true, d:'Generates SQL to revert future changes up to the <liquibaseCommandValue> tag.'],
                [c:'future-rollback-sql', rv:false, d:'Generate the raw SQL needed to rollback undeployed changes.'],
                [c:'generate-changelog', rv:false, d:'Generate a Groovu changelog from the current state of the database.'],
                [c:'history', rv:false, d:'List all deployed changesets and their deployment IDs.'],
                [c:'list-locks', rv:false, d:'List the hostname, IP address, and timestamp of the Liquibase lock record.'],
                [c:'mark-next-changeset-ran', rv:false, l:'markNextChangeSetRan', d:'Mark the next change you apply as executed in the database.'],
                [c:'mark-next-changeset-ran-sql', rv:false, l:'markNextChangeSetRanSql', d:'Write SQL to mark the next change you apply as executed in the database.'],
                [c:'register-changelog', rv:false, l:'registerChangeLog', d:'Register the changelog with a Liquibase Hub project.'],
                [c:'release-locks', rv:false, d:'Remove the Liquibase lock record from the DATABASECHANGELOG table.'],
                [c:'rollback', rv:true, d:'Rollback changes made to the database since the the <liquibaseCommandValue> tag was applied.'],
                [c:'rollback-count', rv:true, d:'Rollback the last <liquibaseCommandValue> changes.'],
                [c:'rollback-count-sql', rv:true, d:'Write SQL to roll back the last <liquibaseCommandValue> changes.'],
                [c:'rollback-one-changeet', rv:true, l:'rollbackOneChangeSet', d:'Roll back the specific <liquibaseCommandValue> changeset, without rolling back changesets deployed before or afterwards. (Liquibase Pro key required)'],
                [c:'rollback-one-changeet-sql', rv:true, l:'rollbackOneChangeSetSql', d:'Write SQL to roll back the specific <liquibaseCommandValue> changeset, without rolling back changesets deployed before or afterwards. (Liquibase Pro key required)'],
                [c:'rollback-one-update', rv:false, d:'Rollback one update from the database (Liquibase Pro key required).'],
                [c:'rollback-one-update-sql', rv:false, d:'Write SQL to rollback one update from the database (Liquibase Pro key required).'],
                [c:'rollback-sql', rv:true, d:'Write SQL to roll back the database to the state it was in when the <liquibaseCommandValue> tag was applied.'],
                [c:'rollback-tTo-date', rv:true, d:'Rollback changes made to the database since the <liquibaseCommandValue> date/time.'],
                [c:'rollback-tTo-date-sql', rv:true, d:'Write SQL to rollback changes made to the database since the <liquibaseCommandValue> date/time.'],
                [c:'snapshot', rv:false, d:'Capture the current state of the database.'],
                [c:'snapshot-reference', rv:false, d:'Capture the current state of the reference database.'],
                [c:'status', rv:false, d:'Generate a list of pending changesets.'],
                [c:'sync-hub', rv:false, d:'Synchronize the local DatabaseChangeLog table with Liquibase Hub.'],
                [c:'tag', rv:true, d:'Mark the current database state with the <liquibaseCommandValue>.'],
                [c:'tag-exists', rv:true, d:'Verify the existence of the <liquibaseCommandValue> tag.'],
                [c:'unexpected-changesets', rv:false, l:'unexpectedChangeSets', d:'Generate a list of changesets that have been executed but are not in the current changelog.'],
                [c:'update', rv:false, d:'Deploy any changes in the changelog file that have not been deployed.'],
                [c:'update-count', rv:true, d:'Deploy the next <liquibaseCommandValue> changes from the changelog file.'],
                [c:'update-count-sql', rv:true, d:'Generate the SQL to deploy the next <liquibaseCommandValue> changes from the changelog file..'],
                [c:'update-sql', rv:false, d:'Writes SQL to update the database to the current version to STDOUT.'],
                [c:'update-testing-rollback', rv:false, d:'Updates database, then rolls back changes before updating again. Useful for testing rollback support.'],
                [c:'update-to-tag', rv:true, d:'Deploy changes from the changelog file to the <liquibaseCommandValue> tag.'],
                [c:'update-to-tag-sql', rv:true, d:'Generate the SQL to deploy changes from the changelog file to the <liquibaseCommandValue> tag]'],
                [c:'validate', rv:false, d:'Validate the changelog for errors.']
        ].each { liquibaseCommand ->
            def commandName = liquibaseCommand.c
            // The legacy command is usually just the camel case version of the command, unless
            // we've been given an override.
            def legacyName = liquibaseCommand.l? liquibaseCommand.l :
                    commandName.replaceAll( "(-)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )

            // Match the task name to the legacy command.
            def taskName = legacyName

            // Fix the task name if we have a task prefix.
            if ( project.hasProperty('liquibaseTaskPrefix') ) {
                taskName = project.liquibaseTaskPrefix + taskName.capitalize()
            }
            project.tasks.register(taskName, LiquibaseTask) {
                group = 'Liquibase'
                description = liquibaseCommand.d
                command = commandName
                legacyCommand = legacyName
                requiresValue = liquibaseCommand.rv
            }
        }
    }
}
