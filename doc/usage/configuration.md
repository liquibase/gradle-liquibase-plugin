# Configuring the plugin

Configuring the plugin involves understanding three basic items: commands, parameters, and
activities.

- A command is a liquibase command, such as `dropAll`, `update`, or `diffChangelog`.These are the
  things you want Liquibase to do, as described in the
  [Liquibase Command Documentation](https://docs.liquibase.com/commands/home.html).  The
  Liquibase Gradle plugin creates a task for each Liquibase Command, which is how users tell Gradle
  what it wants Liquibase to do.

- Parameters refer to the parameters that get sent to Liquibase to configure how Liquibase will run
  the command.  This includes things like the database credentials, the location of the changelog
  file, etc.  Parameters are also documented in the
  [Liquibase Command Documentation](https://docs.liquibase.com/commands/home.html).  Parameters are
  typically set up in an "activity" section of the `liquibase` block.

- Activities are the trickiest of the three.  A command is a low level task to be done, while an
  activity is a more high level, broad category, and can be used to run the same command in
  different ways.  For example, deploying an application might involve updating the application's
  schema, but it might also involve inserting metadata about the application in a registry.  The
  plugin achieves this by allowing users to define 2 activities, each referring to different
  databases and changelogs.  Activities can also be thought of as a collection of parameters that
  need to be grouped together.


With these concepts in mind, parameters for Liquibase commands are configured in the
`liquibase` block inside the build.gradle file, using one or more "activities", each defining a
series of Liquibase parameters.  Any method in an "activity" is assumed to be a Liquibase command
line parameter.  For example, including `changelogFile 'myfile.groovy'` in an activity does the same
thing as `--changelog-file=myfile.groovy` would do on the command line.  Including
`difftypes 'data'` in an activity does the same thing as `difftypes=data` would do on the command
line, etc.  The Liquibase documentation details all the valid command line parameters, though the
documentation tends to use kebab-case for the parameters, which it calls attributes.  The gradle
plugin, in keeping with Groovy conventions, uses camelCase, and translates as necessary before
calling Liquibase.

Some parameters changed in Liquibase 4.4, for example, `changeLogFile` became `changelogFile` with a
lowercase "l".  The plugin will automatically convert pre-4.4 names for a time to support backwards
compatibility, but will print a warning to update the parameter.

The `liquibase` block also has an
optional "runList", which determines which activities are run for each task.  If no runList is
defined, the Liquibase Plugin will run all the activities.  NOTE: the order of execution when there
is no runList is not guaranteed.

The arguments in `build.gradle` can be overriden on the command line using the `liquibaseExtraArgs`
property.  For example, if you wanted to override the log level for a single run, you could run
`gradlew -PliquibaseExtraArgs="logLevel=debug"` and it would print debug messages from liquibase.
This property can also be used to add extra arguments that weren't in any `activity` blocks.
Multiple arguments can be specified by separating them with a comma, like this:
`-PliquibaseExtraArgs="logLevel=debug,username=me`.  Due to limitations of Gradle, spaces are not
allowed in the value of the liquibaseExtraArgs property.

The `liquibase` block can also set two properties; `mainClassName` and `jvmArgs`.

The `mainClassName` property tells the plugin the name of the class to invoke in order to run
Liquibase.  By default, the plugin determines the version of Liquibase being used and sets this
value to either `liquibase.integration.commandline.LiquibaseCommandLine` for version 4.4+, or
`liquibase.integration.commandline.Main` for earlier versions.  This value can be set to call other
classes instead, such as the plugin's own `org.liquibase.gradle.OutputEnablingLiquibaseRunner` which
fixes a Liquibase 3.6 logging issue.  You will need to make sure whatever class you use with
`mainClassName` can be found in one of the `liquibaseRuntime` dependencies.

The `jvmArgs` property tells the plugin what JVM arguments to set when forking the Liquibase
process, and defaults to an empty array, which is usually fine.

If you are using `liquibase` in a subproject structure, due to a limitation in liquibase, you will
need to override the `user.dir` using the `jvmArgs`.