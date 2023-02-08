package org.liquibase.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class LiquibasePluginTest {
    Project project

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
    }

    /**
     * Typically, a plugin is applied by name, but Gradle supports applying by type.  Prove that it
     * works.  We aren't going to go nuts here, just look for one task that takes an argument, and
     * one that doesn't
     */
    @Test
    void applyPluginByType() {
        project.apply plugin: org.liquibase.gradle.LiquibasePlugin
        assertTrue("Project is missing plugin", project.plugins.hasPlugin(LiquibasePlugin))
        project.repositories.configure { mavenCentral() }
        project.dependencies.add(LiquibasePlugin.LIQUIBASE_RUNTIME_CONFIGURATION, "org.liquibase:liquibase-core:4.4.0")
        // the tag task takes an arg...
        def task = project.tasks.findByName('tag')
        assertNotNull("Project is missing tag task", task)
        assertTrue("tag task is the wrong type", task instanceof LiquibaseTask)
        assertTrue("tag task should be enabled", task.enabled)
        assertEquals("tag task has the wrong command", "tag", task.liquibaseCommand.command)
        // and the update task does not.
        task = project.tasks.findByName('update')
        assertNotNull("Project is missing update task", task)
        assertTrue("update task is the wrong type", task instanceof LiquibaseTask)
        assertTrue("update task should be enabled", task.enabled)
        assertEquals("update task has the wrong command", "update", task.liquibaseCommand.command)
    }

    /**
     * Apply the plugin by name and make sure it creates tasks.  We don't go nuts here, just look
     * for a task that takes an argument, and one that doesn't
     */
    @Test
    void applyPluginByName() {
        project.apply plugin: 'org.liquibase.gradle'
        assertTrue("Project is missing plugin", project.plugins.hasPlugin(LiquibasePlugin))
        project.repositories.configure { mavenCentral() }
        project.dependencies.add(LiquibasePlugin.LIQUIBASE_RUNTIME_CONFIGURATION, "org.liquibase:liquibase-core:4.4.0")
        // the tag task takes an arg...
        def task = project.tasks.findByName('tag')
        assertNotNull("Project is missing tag task", task)
        assertTrue("tag task is the wrong type", task instanceof LiquibaseTask)
        assertTrue("tag task should be enabled", task.enabled)
        assertEquals("tag task has the wrong command", "tag", task.liquibaseCommand.command)
        // and the update task does not.
        task = project.tasks.findByName('update')
        assertNotNull("Project is missing update task", task)
        assertTrue("update task is the wrong type", task instanceof LiquibaseTask)
        assertTrue("update task should be enabled", task.enabled)
        assertEquals("update task has the wrong command", "update", task.liquibaseCommand.command)
    }

    /**
     * Apply the plugin by name, but this time, specify a value for the liquibaseTaskPrefix and make
     * sure it changes the task names accordingly.  We don't go nuts here, just look for a task that
     * takes an argument, and one that doesn't.  We also make sure that while the task names are
     * changed, the commands they run are not.
     */
    @Test
    void applyPluginByNameWithPrefix() {
        project.ext.liquibaseTaskPrefix = 'liquibase'
        project.apply plugin: 'org.liquibase.gradle'
        assertTrue("Project is missing plugin", project.plugins.hasPlugin(LiquibasePlugin))
        project.repositories.configure { mavenCentral() }
        project.dependencies.add(LiquibasePlugin.LIQUIBASE_RUNTIME_CONFIGURATION, "org.liquibase:liquibase-core:4.4.0")
        // the tag task takes an arg...
        def task = project.tasks.findByName('liquibaseTag')
        assertNotNull("Project is missing tag task", task)
        assertTrue("tag task is the wrong type", task instanceof LiquibaseTask)
        assertTrue("tag task should be enabled", task.enabled)
        assertEquals("tag task has the wrong command", "tag", task.liquibaseCommand.command)
        // and the update task does not.
        task = project.tasks.findByName('liquibaseUpdate')
        assertNotNull("Project is missing update task", task)
        assertTrue("update task is the wrong type", task instanceof LiquibaseTask)
        assertTrue("update task should be enabled", task.enabled)
        assertEquals("update task has the wrong command", "update", task.liquibaseCommand.command)

        // Make sure the standard tasks didn't get created, since we created them with different
        // names.
        task = project.tasks.findByName('tag')
        assertNull("We shouldn't have a tag task", task)
        task = project.tasks.findByName('update')
        assertNull("We shouldn't have an update task", task)
    }

    @Test
    void checkVersionDetection() {
        def task = configureForVersion("4.5.0")
        assertEquals( "liquibase.integration.commandline.LiquibaseCommandLine", task.mainClass.get())
    }

    @Test
    void checkVersionDetection_oldVersion() {
        def task = configureForVersion("4.3.0")
        assertEquals("liquibase.integration.commandline.Main", task.mainClass.get())
    }

    @Test
    void checkVersionDetection_customMainClass() {
        def task = configureForVersion("4.3.0") {
            (it.extensions.liquibase as LiquibaseExtension).mainClassName = "com.example.CustomMain"
        }
        assertEquals("com.example.CustomMain", task.mainClass.get())
    }

    private LiquibaseTask configureForVersion(String version, Closure closure = {}) {
        project.apply plugin: 'liquibase'
        assertTrue("Project is missing plugin", project.plugins.hasPlugin(LiquibasePlugin))
        project.configurations.getByName(LiquibasePlugin.LIQUIBASE_RUNTIME_CONFIGURATION) {
            dependencies.add(
                    project.dependencies.create("org.liquibase:liquibase-core:$version")
            )
        }
        project.repositories.mavenCentral()
        closure(project)

        LiquibaseTask task = project.tasks.findByName('update')
        assertNotNull("Project is missing update task", task)
        task.configure {}
        return task
    }
}
