/*
 * Copyright 2011-2017 Tim Berglund and Steven C. Saliman
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

class LiquibasePlugin
		implements Plugin<Project> {

	public static final String LIQUIBASE_RUNTIME_CONFIGURATION = "liquibaseRuntime";

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
				'update': 'Updates the database to the current version.',
				'updateSQL': 'Writes SQL to update the database to the current version to STDOUT.',
				'futureRollbackSQL': 'Writes SQL to roll back the database to the current state after the changes in the changeslog have been applied.',
				'updateTestingRollback': 'Updates the database, then rolls back changes before updating again.',
				'listLocks': 'Lists who currently has locks on the database changelog.',
				'dropAll': 'Drops all database objects owned by the user. Note that functions, procedures and packages are not dropped (Liquibase limitation)',
				'releaseLocks': 'Releases all locks on the database changelog.',
				'validate': 'Checks the changelog for errors.',
				'diff': 'Writes description of differences to standard out.',
				'diffChangeLog': 'Writes Change Log to update the database to the reference database to standard out',
				'snapshot': 'Writes the current state of the database to standard out',
				'snapshotReference': 'Writes the current state of the referenceUrl database to standard out',
				'clearChecksums': 'Removes all saved checksums from the database. On next run checksums will be recomputed.  Useful for "MD5Sum Check Failed" errors.',
				'changelogSync': 'Mark all changes as executed in the database.',
				'changelogSyncSQL': 'Writes SQL to mark all changes as executed in the database to STDOUT.',
				'markNextChangesetRan': 'Mark the next change set as executed in the database.',
				'markNextChangesetRanSQL': 'Writes SQL to mark the next change set as executed in the database to STDOUT.',
				'status': 'Outputs count (list if liquibaseCommandValue is "--verbose") of unrun change sets.',
				'generateChangelog': 'Writes Change Log groovy to copy the current state of the database to standard out.',
				'unexpectedChangeSets': 'Outputs count (list if liquibaseCommandValue is "--verbose") of changesets run in the database that do not exist in the changelog.'

		].each { taskName, taskDescription ->
			def commandName = taskName
			if ( project.hasProperty('liquibaseTaskPrefix') ) {
				taskName = project.liquibaseTaskPrefix + taskName.capitalize()
			}
			project.task(taskName, type: LiquibaseTask) {
				group = 'Liquibase'
				description = taskDescription
				command = commandName
			}
		}

		// Create tasks that do require a value.
		[
				'updateCount': 'Applies the next <liquibaseCommandValue> change sets.',
				'updateCountSql': 'Writes SQL to apply the next <liquibaseCommandValue> change sets to STDOUT.',
				'updateToTag': 'Updates the database to the changeSet with the <liquibaseCommandValue> tag',
				'updateToTagSQL': 'Writes (to standard out) the SQL to update to the changeSet with the <liquibaseCommandValue> tag',
				'rollback': 'Rolls back the database to the state it was in when the <liquibaseCommandValue> tag was applied.',
				'rollbackToDate': 'Rolls back the database to the state it was in at the <liquibaseCommandValue> date/time.',
				'rollbackCount': 'Rolls back the last <liquibaseCommandValue> change sets.',
				'rollbackSQL': 'Writes SQL to roll back the database to the state it was in when the <liquibaseCommandValue> tag was applied to STDOUT.',
				'rollbackToDateSQL': 'Writes SQL to roll back the database to the state it was in at the <liquibaseCommandValue> date/time to STDOUT.',
				'rollbackCountSQL': 'Writes SQL to roll back the last <liquibaseCommandValue> change sets to STDOUT.',
				'futureRollbackCountSQL': 'Writes SQL to roll back <liquibaseCommandValue> changes the database after the changes in the changelog have been applied.',
				'tag': 'Tags the current database state with <liquibaseCommandValue> for future rollback.',
				'tagExists': 'Checks whether the tag given in <liquibaseCommandValue> is already existing.',
				'executeSql': 'Executes SQL in the database',
				'calculateCheckSum': 'Calculates and prints a checksum for the <liquibaseCommandValue> changeset with the given id in the format filepath::id::author.',
				'dbDoc': 'Generates Javadoc-like documentation based on current database and change log to the <liquibaseCommandValue> directory.'
		].each { taskName, taskDescription ->
			def commandName = taskName
			if ( project.hasProperty('liquibaseTaskPrefix') ) {
				taskName = project.liquibaseTaskPrefix + taskName.capitalize()
			}
			project.task(taskName, type: LiquibaseTask) {
				group = 'Liquibase'
				description = taskDescription
				command = commandName
				requiresValue = true
			}
		}
	}
}
