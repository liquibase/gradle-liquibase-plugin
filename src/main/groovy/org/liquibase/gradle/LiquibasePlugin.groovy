/*
 * Copyright 2011-2021 Tim Berglund and Steven C. Saliman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
	 * Create all of the liquibase tasks and add them to the project.  If the
	 * liquibaseTaskPrefix is set, add that prefix to the task names.
	 * @param project the project to enhance
	 */
	void applyTasks(Project project) {
		// Create tasks that don't require a value.
		[
				'changelogSync': 'Mark all changes as executed in the database.',
				'changelogSyncSql': 'Output the raw SQL used by Liquibase when running changelogSync.',
				'clearChecksums': 'Remove all saved checksums from the database. On next run checksums will be recomputed.  Useful for "MD5Sum Check Failed" errors.',
				'deactivateChangeLog': 'Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub.',
                'diff': 'Compare two databases and write differences to STDOUT.',
				'diffChangeLog': 'Compare two databases to produce changesets and write them to a changelog file',
				'dropAll': 'Drop all database objects owned by the user. Note that functions, procedures and packages are not dropped (Liquibase limitation)',
				'futureRollbackSql': 'Generate the raw SQL needed to rollback undeployed changes.',
				'generateChangelog': 'Generate a Groovu changelog from the current state of the database.',
				'history': 'List all deployed changesets and their deployment IDs.',
				'listLocks': 'List the hostname, IP address, and timestamp of the Liquibase lock record.',
				'markNextChangesetRan': 'Mark the next change you apply as executed in the database.',
				'markNextChangesetRanSql': 'Write SQL to mark the next change you apply as executed in the database.',
				'registerChangeLog': 'Register the changelog with a Liquibase Hub project.',
				'releaseLocks': 'Remove the Liquibase lock record from the DATABASECHANGELOG table.',
				'rollbackOneUpdate': 'Rollback one update from the database (Liquibase Pro key required).',
		        'rollbackOneUpdateSql': 'Write SQL to rollback one update from the database (Liquibase Pro key required).',
				'snapshot': 'Capture the current state of the database.',
				'snapshotReference': 'Capture the current state of the reference database.',
				'status': 'Generate a list of pending changesets.',
				'syncHub': 'Synchronize the local DatabaseChangeLog table with Liquibase Hub.',
				'unexpectedChangeSets': 'Generate a list of changesets that have been executed but are not in the current changelog.',
				'update': 'Deploy any changes in the changelog file that have not been deployed.',
				'updateSql': 'Writes SQL to update the database to the current version to STDOUT.',
				'updateTestingRollback': 'Updates database, then rolls back changes before updating again. Useful for testing rollback support.',
				'validate': 'Validate the changelog for errors.'
		].each { taskName, taskDescription ->
			def commandName = taskName
			if ( project.hasProperty('liquibaseTaskPrefix') ) {
				taskName = project.liquibaseTaskPrefix + taskName.capitalize()
			}
			project.tasks.register(taskName, LiquibaseTask) {
				group = 'Liquibase'
				description = taskDescription
				command = commandName
			}
		}

		// Create tasks that do require a value.
		[
				'calculateCheckSum': 'Calculate and print a checksum for the <liquibaseCommandValue> changeset.',
				'changelogSyncToTag': 'Mark all undeployed changesets up to and including the <liquibaseCommandValue> tag as executed in your database.',
				'changelogSyncToTagSql': 'Output the raw SQL used by Liquibase when running the changelogSyncToTag command.',
				'checks': 'Execute the <liquibaseCommandValue> quality check',
				'dbDoc': 'Generates Javadoc-like documentation for the existing database and changelogs to the <liquibaseCommandValue> directory.',
				'executeSql': 'Execute a SQL string or file given in <liquibaseCommandValue> in this format: -PliquibaseCommandValue="--sql=select 1" or -PliquibaseCommandValue="--sqlFile=myfile.sql"',
				'futureRollbackCountSql': 'Generates SQL to sequentially revert <liquibaseCommandValue> changes the database.',
				'futureRollbackFromTagSql': 'Generates SQL to revert future changes up to the <liquibaseCommandValue> tag.',
				'rollback': 'Rollback changes made to the database since the the <liquibaseCommandValue> tag was applied.',
				'rollbackCount': 'Rollback the last <liquibaseCommandValue> changes.',
				'rollbackCountSql': 'Write SQL to roll back the last <liquibaseCommandValue> changes.',
				'rollbackOneChangeSet': 'Roll back the specific <liquibaseCommandValue> changeset, without rolling back changesets deployed before or afterwards. (Liquibase Pro key required)',
				'rollbackOneChangeSetSql': 'Write SQL to roll back the specific <liquibaseCommandValue> changeset, without rolling back changesets deployed before or afterwards. (Liquibase Pro key required)',
                'rollbackSql': 'Write SQL to roll back the database to the state it was in when the <liquibaseCommandValue> tag was applied.',
				'rollbackToDate': 'Rollback changes made to the database since the <liquibaseCommandValue> date/time.',
				'rollbackToDateSql': 'Write SQL to rollback changes made to the database since the <liquibaseCommandValue> date/time.',
				'tag': 'Mark the current database state with the <liquibaseCommandValue>.',
				'tagExists': 'Verify the existence of the <liquibaseCommandValue> tag.',
				'updateCount': 'Deploy the next <liquibaseCommandValue> changes from the changelog file.',
				'updateCountSql': 'Generate the SQL to deploy the next <liquibaseCommandValue> changes from the changelog file..',
				'updateToTag': 'Deploy changes from the changelog file to the <liquibaseCommandValue> tag.',
				'updateToTagSql': 'Generate the SQL to deploy changes from the changelog file to the <liquibaseCommandValue> tag'
		].each { taskName, taskDescription ->
			def commandName = taskName
			if ( project.hasProperty('liquibaseTaskPrefix') ) {
				taskName = project.liquibaseTaskPrefix + taskName.capitalize()
			}
			project.tasks.register(taskName, LiquibaseTask) {
				group = 'Liquibase'
				description = taskDescription
				command = commandName
				requiresValue = true
			}
		}
	}
}
