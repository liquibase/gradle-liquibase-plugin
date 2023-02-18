package org.liquibase.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.liquibase.gradle.liquibase.command.ExecuteSqlCommand
import org.liquibase.gradle.liquibase.command.ExecuteSqlFileCommand
import org.liquibase.gradle.liquibase.command.UpdateCommand

import static org.junit.Assert.assertEquals

/**
 * Unit tests for the {@link ArgumentBuilder}
 *
 * @author Steven C. Saliman
 */
class ArgumentBuilderTest {
    def activity
    def command
    def actualArgs
    def expectedArgs

    Project project

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.ext.liquibaseCommandValue = "myTag"
        activity = new Activity("main")

        // Add some command arguments.  One of them needs to be unsupported, and one needs to be
        // a boolean.
        activity.changelogFile "myChangelog"  // needed to test proper handling of "-D" args
        activity.username "myUsername"
        activity.password "myPassword"  // This one will be unsupported.
        activity.force()  // Boolean

        // Add some post-command arguments. Like with the pre-command args, we need one unsupported
        // and one boolean.
        activity.excludeObjects "myExcludes"  // unsupported
        activity.includeObjects "myIncludes"
        activity.verbose()  // boolean

        // Add some global arguments.  This can't be anything that exists in LiquiabseCommand.
        activity.globalArg "globalValue"

        // some changelog params
        activity.changeLogParameters(["param1": "value1", "param2": "value2"])

        // Set up a command, with supported that exclude the ones marked above as "unsupported"
        // We'll also set up a valueArgument, but  we won't make it required.
        command = new UpdateCommand() // We needed a command.  It doesn't matter what it was.
        command.command = "my-command"
        command.commandArguments = ["changelogFile", "username", "force", "includeObjects", "verbose", "tag"]
        command.valueArgument = "tag"
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line.  Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsFullArguments() {
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line.  This is the same test as before, except that we use the legacy value for the
     * changeLogFile in the activity.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsFullArgumentsWithLegacy() {
        activity = new Activity("main")

        // Add some command arguments.  One of them needs to be unsupported, and one needs to be
        // a boolean.
        activity.changeLogFile "myChangelog"  // needed to test proper handling of "-D" args
        activity.username "myUsername"
        activity.password "myPassword"  // This one will be unsupported.
        activity.force()  // Boolean

        // Add some post-command arguments. Like with the pre-command args, we need one unsupported
        // and one boolean.
        activity.excludeObjects "myExcludes"  // unsupported
        activity.includeObjects "myIncludes"
        activity.verbose()  // boolean

        // Add some global arguments.  This can't be anything that exists in LiquiabseCommand.
        activity.globalArg "globalValue"

        // some changelog params
        activity.changeLogParameters(["param1": "value1", "param2": "value2"])

        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))

    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but the command doesn't support the changelog-file.  We should omit "-D" args, because
     * they only get sent with changelog-file.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsNoChangelog() {
        command.commandArguments = ["username", "force", "includeObjects", "verbose", "tag"]
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.  Did we forget to filter out the changelog parms when not using changelog-file?",
                expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but the command is drop-all.  This tests that we work around
     * https://github.com/liquibase/liquibase/issues/3380 by omitting the changelog when we are
     * running drop-all.  We should also omit the "-D" args.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsDropAll() {
        // The drop-all command has special handling.
        command.command = "drop-all"
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--username=myUsername",
                "--force",
                "drop-all",
                "--include-objects=myIncludes",
                "--verbose",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.  Did we forget to filter out the changelog and changelog parms with drop-all?",
                expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but the command doesn't have a valueArgument.  In this case, we expect the value
     * argument to be at the end by itself.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * myTag because the command doesn't have a valueArgument
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsNoValueArg() {
        command.valueArgument = null
        command.commandArguments = ["changelogFile", "username", "force", "includeObjects", "verbose"]

        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line and the activity has a value for the argumentValue too.  This test proves that the
     * command line wins over the activity.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * myTag because the command doesn't have a valueArgument
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseActivityValueArg() {
        activity.tag "activityTag"
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.  Did the command line override the activity value argument?",
                expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line except for the command value, but the activity does have has a value for the
     * argumentValue.  This proves we can set default values.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * myTag because the command doesn't have a valueArgument
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseNoCommandValueActivityValueArg() {
        // We can't remove a property, so make a new project without one.
        project = ProjectBuilder.builder().build()
        activity.tag "activityTag"
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=activityTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.  Did we use the command value from the activity?",
                expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * but we don't set a command value, and a value is required.  Expect an exception.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * myTag because the command doesn't have a valueArgument
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test(expected = LiquibaseConfigurationException)
    void buildLiquibaseNoRequiredValue() {
        // We can't remove a property, so make a new project without one.
        project = ProjectBuilder.builder().build()
        command.requiresValue = true
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line but we don't set a command value for the db-doc command.  This is the only command
     * that has a default value.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * output-dir with the default value
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseNoDbDocCommandValue() {
        // We can't remove a property, so make a new project without one.
        project = ProjectBuilder.builder().build()
        // The db-doc command has special handling
        command.command = "db-doc"
        command.valueArgument = "outputDir"
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "db-doc",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--output-dir=${project.buildDir}/database/docs"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.  Did we use the default value for output-dir with db-doc?",
                expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * but we don't set a command value for a command doesn't require one.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseNoOptionalCommandValue() {
        // We can't remove a property, so make a new project without one.
        project = ProjectBuilder.builder().build()
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.",
                expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but no changelog parameters.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsNoChangeLogParms() {
        activity.changeLogParameters.clear()
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but the command doesn't support any post-command arguments.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsCommandHasNoPostArguments() {
        command.commandArguments = ["changelogFile", "username", "force", "tag"]
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line except for post-command arguments.  Not having any should not cause problems
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.
     */
    @Test
    void buildLiquibaseArgsNoActivityPostCommandArgs() {
        activity = new Activity("main")

        // Add some command arguments.  One of them needs to be unsupported, and one needs to be
        // a boolean.
        activity.changelogFile "myChangelog"  // needed to test proper handling of "-D" args
        activity.username "myUsername"
        activity.password "myPassword"  // This one will be unsupported.
        activity.force()  // Boolean

        // Add some global arguments.  This can't be anything that exists in LiquiabseCommand.
        activity.globalArg "globalValue"

        // some changelog params
        activity.changeLogParameters(["param1": "value1", "param2": "value2"])
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but the command doesn't support any pre-command arguments.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.  Also expect the changelog parameters (-D) to be filtered out because the
     * changelog-file was filtered out.
     */
    @Test
    void buildLiquibaseArgsNoPreCommandArgsSupported() {
        command.commandArguments = ["includeObjects", "verbose", "tag"]
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, except that the activity doesn't provide any pre-command arguments.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.  Also expect the changelog parameters (-D) to be filtered out because the
     * changelog-file was filtered out.
     */
    @Test
    void buildLiquibaseArgsActivityHasNoPreCommandArgs() {
        activity = new Activity("main")

        // Add some post-command arguments. Like with the pre-command args, we need one unsupported
        // and one boolean.
        activity.excludeObjects "myExcludes"  // unsupported
        activity.includeObjects "myIncludes"
        activity.verbose()  // boolean

        // Add some global arguments.  This can't be anything that exists in LiquiabseCommand.
        activity.globalArg "globalValue"

        // some changelog params
        activity.changeLogParameters(["param1": "value1", "param2": "value2"])
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, but the command doesn't support any command arguments at all.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * my-command, which is the command.
     * the "myTag" value without a keyword because there isn't a valueARgument anymore.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.  Also expect the changelog parameters (-D) to be filtered out because the
     * changelog-file was filtered out.
     */
    @Test
    void buildLiquibaseArgsCommandSupportsNoArguments() {
        command.commandArguments = []
        command.valueArgument = null
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "my-command",
                "myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we the activity doesn't define any arguments at all.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * my-command, which is the command.
     * the "myTag" value without a keyword because there isn't a valueARgument anymore.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments.  Also expect the changelog parameters (-D) to be filtered out because the
     * changelog-file was filtered out.
     */
    @Test
    void buildLiquibaseArgsActivityHasNoArgs() {
        activity = new Activity("main")

        // Add some global arguments.  This can't be anything that exists in LiquiabseCommand.
        activity.globalArg "globalValue"

        // some changelog params
        activity.changeLogParameters(["param1": "value1", "param2": "value2"])
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "my-command",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have absolutely nothing.  Expect to just send the command
     */
    @Test
    void buildLiquibaseArgsWithNothing() {
        // new project to clear the command value
        project = ProjectBuilder.builder().build()
        // new activity and we'll even clear out the default argument.
        activity = new Activity("main")
        activity.changeLogParameters = [:]
        activity.arguments = [:]

        expectedArgs = [
                "my-command",
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, and the activity defines an output file.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * output-file with a value
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments, and the output-file goes after global arguments because it is a global
     * argument in Liquibase.
     */
    @Test
    void buildLiquibaseArgsOutputFileInActivity() {
        activity.outputFile "myFile"
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--output-file=myFile",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments.  Did we forget to add the output file?", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line.
     *
     * Expect the following arguments in exactly this order.
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * changelog-file with a value
     * username with a value
     * force without one
     * output-file with a value
     * my-command, which is the command.
     * include-objects with a value
     * verbose without one
     * the two -D parameters.
     * --tag with a value because the command value comes last.
     *
     * Expect password and exclude-objects to be filtered out because the command doesn't support
     * those arguments, and the output-file goes after global arguments because it is a global
     * argument in Liquibase.
     */
    @Test
    void buildLiquibaseArgsOutputFileInProperties() {
        project.ext.liquibaseOutputFile = "myFile"
        activity.outputFile "activityFile"
        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--output-file=myFile",
                "--changelog-file=myChangelog",
                "--username=myUsername",
                "--force",
                "my-command",
                "--include-objects=myIncludes",
                "--verbose",
                "-Dparam1=value1",
                "-Dparam2=value2",
                "--tag=myTag"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, and we're dealing with the execute-sql command.  This will test the special handling
     * of the execute-sql command.  Expect the following arguments in exactly this order:
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * username with a value
     * password with a value
     * execute-sql, which is the command.
     * --sql with a value because the command value comes last.
     *
     * Expect other arguments to be filtered out because the command doesn't support them.
     */
    @Test
    void buildLiquibaseArgsExecuteSql() {
        command = new ExecuteSqlCommand()
        project.ext.liquibaseCommandValue = "mySql"

        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--username=myUsername",
                "--password=myPassword",
                "execute-sql",
                "--sql=mySql"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }

    /**
     * Test building arguments when we have all the argument types that could exist in a command
     * line, and we're dealing with the execute-sql-file command.  This will test the special
     * handling of the execute-sql-file command.  Expect the following arguments in exactly this
     * order:
     * log-level because the Activity has a default value.
     * global-arg because global arguments come first.
     * username with a value
     * password with a value
     * execute-sql, which proves that "execute-sql-file" will be properly replaced..
     * --sql-file with a value because the command value comes last.
     *
     * Expect other arguments to be filtered out because the command doesn't support them.
     */
    @Test
    void buildLiquibaseArgsExecuteSqlFile() {
        command = new ExecuteSqlFileCommand()
        project.ext.liquibaseCommandValue = "mySqlFile"

        expectedArgs = [
                "--log-level=info",
                "--global-arg=globalValue",
                "--username=myUsername",
                "--password=myPassword",
                "execute-sql",
                "--sql-file=mySqlFile"
        ]
        actualArgs = ArgumentBuilder.buildLiquibaseArgs(project, activity, command, "4.4.0")
        // For some reason, comparing arrays, doesn't work right, so join into single strings.
        assertEquals("Wrong arguments", expectedArgs.join(" "),  actualArgs.join(" "))
    }


}
