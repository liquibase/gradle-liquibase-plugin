Liquibase Gradle Plugin
-----------------------

A plugin for [Gradle](http://gradle.org) that allows you to use [Liquibase](http://liquibase.org)
to manage your database upgrades.  This project was originally created by Tim Berglund, and is
currently maintained by Steve Saliman.

News
----

**IMPORTANT:** Additional configuration will be required to use version 2.1.0+ of this plugin with
Liquibase 4.4.0+.  Liquibase now uses the picocli library to parse options, but for some reason that
library isn't a transitive dependency of Liquibase itself, so if you want to use this plugin with
Liquibase 4.4.0+, you'll have to add the `liquibaseRuntime 'info.picocli:picocli:4.6.1'` dependency
to your build.gradle file.

### February 20, 2022
This version of the plugin has some important changes

- The older plugin id is no longer supported.  To apply this plugin now, you must use  
  `org.liquibase.gradle`.  

- The plugin creates tasks that line up with the newer Liquibase 4.4+ commands.  To create tasks
  that match the older pre 4.4 commands, to support backwards compatibility in CI/CD pipelines for
  example, simply add `-PliquibaseCreateLegacyTasks` to the gradle command.  This can be done 
  regardless of the version of Liquibase being used.  This support will be removed in the future.
  It is helpful to keep in mind that while it is convenient for the task to match the Liquibase
  commands, it is not necessary, so Liquibase 4.4 tasks can still be used with older versions of
  Liquibase, the plugin will translate commands and arguments automatically.

- There is a new `executeSqlFile` task for executing SQL from a file.  The `executeSql` task now
  only executes the SQL given in the `liquibaseCommandValue` property, and `executeSqlFile` executes
  the SQL given in the filename specified by the `liquibaseCommandValue` property.

- The plugin now sends the newer kebab case commands to Liquibase when it detects newer versions in
  of Liquibase in the classpath.  For example, it uses `drop-all` when it detects version 4.4+
  instead of the legacy `dropAll` command that it sends to older versions of Liquibase.  

- An output file can be specified on the command line, for tasks that use one, with the 
  `-PliquibaseOutputFile=someFile` property.  This will override the `outputFile` specified in the
  `activity` block of your build.gradle file.

### December 20, 2021
Fixed the Code that detects the version of liquibase in use at the time the liquibase tasks run.  

### November 13, 2021

Release 2.1.0 adds support for Liquibase 4.4.0 and 4.5.0. Liquibase 4.4.0 made extensive changes to
the way it processes command line arguments.  Liquibase now uses the picocli library to parse
options, but for some reason that library isn't a transitive dependency of Liquibase itself, so if
you want to use this plugin with Liquibase 4.4.0+, you'll have to add the 
`liquibaseRuntime 'info.picocli:picocli:4.6.1'` dependency to your build.gradle file.  

Liquibase now has 2 "Main" classes and this plugin chooses the best one based on the version of
Liquibase it detects.  You can still set a mainClassName, in the liquibase block of your
build.gradle file, but it will most likely fail in Liquibase 4.4+. 

There is also a subtle change in the way "SQL" tasks get created.  Tasks that ended with "SQL" now
end with "Sql".  For example `updateSQL` is now `updateSql`.  Since neither Gradle nor Liquibase
seems to pay too much attention to case, this should not cause any breaking changes for now, but as
Liquibase itself transitions from camelCase commands to kebab case commands, this may become
important in the future, and this change will make it easier to pass the right thing to Liquibase if
and when Liquibase ever stops supporting camel case.
 
### March 6, 2021
Liquibase version 4.3.0 has a bug that causes the gradle plugin to break.  This appears to be fixed
in Liquibase 4.3.1.

### September 5, 2020
Liquibase 4.0.0 is out, and the initial testing shows that it is compatible with the Liquibase
Gradle plugin.
 
### June 6, 2020
Release 2.0.4 is a minor release that fixes an issue that was preventing debugging in IntelliJ Idea
(#72), and an issue with Groovy dependencies (Issue #74).

### May 24, 2020
Release 2.0.3 is a minor release that fixes an issue caused by changes made in Gradle 6.4.  These
changes were tested with Gradle 5.4, and are backwards compatible at least that far back.

### November 24, 2019
Release 2.0.2 of the plugin fixes an issue with some command parameters like the `snapshotFormat`
parameter of the `snapshot` command, though it should be noted that Liquibase 3.8.1 appears to have
broken json support for the snapshot command.

### November 23, 2019
Liquibase released version 3.8.1, which appears to fix the `logLevel` bug in 
[CORE-3220)](https://liquibase.jira.com/browse/CORE-3220).  It does not fix
[CORE-3643](https://liquibase.jira.com/browse/CORE-3463), so validateXYZ attributes of a constraint
still won't work.

Gradle has released version 6.0, and the Liquibase Gradle Plugin appears to work just fine with it.

### September 21, 2019
Liquibase released versions 3.7 and 3.8.  Neither of which require any changes from the Gradle
plugin to work properly, but there are three issues with the newer Liquibase versions that could
affect users of this plugin:

1. Liquibase 3.7+ made a change that broke the Groovy DSL.  If you're using the Groovy DSL for your
  change sets, you'll need to use version 2.1.0 of the liquibase-groovy parser.

2. Due to a bug in Liquibase ([CORE-3643](https://liquibase.jira.com/browse/CORE-3463)), The Groovy
  DSL won't be able to parse the new validateXYZ attributes of a constraint.  There is a pull
  request to fix the issue.

3. Liquibase 3.7 made more changes to the way it does logging which made the console output issues
  in [CORE-3220)](https://liquibase.jira.com/browse/CORE-3220) even worse, and broke the proxy
  class I wrote to workaround the issue in Liquibase 3.6.  I've submitted a
  [Pull Request](https://github.com/liquibase/liquibase/pull/918) to resolve the issue, but until
  Liquibase is built with the fix, console output will be completely broken in version 3.7+
 
### November 24, 2018
Release 2.0.2 will fix a bug with the plugin's handling of System properties.  All System properties
that are in effect when Gradle runs Liquibase will now be seen by Liquibase.  Previously, System
properties were not inherited by the forked JVM that runs Liquibase.
 
### September 3, 2018
Release 2.0.1 is a minor release that removes the CVE-2016-6814 vulnerability by updating the Groovy
dependency.

### July 14, 2018
We're pleased to announce the release of version 2.0.0 of the Liquibase Gradle plugin, with much
thanks to Jasper de Vries (@litpho).  

**This has breaking changes** so please read all the information in this section before upgrading.

Version 2.0.0 Changes the way the plugin sets up the classpath when running Liquibase.  This allows
us to isolate the classpath Liquibase uses from the one Gradle is using.  Note that this is a
breaking change!  Builds will not work without first fixing your build scripts to set up the
classpath. 

Prior to version 2.0.0, you would need to include the plugin and the database drivers in the
`buildscript` block.  As of version 2.0.0, only the plugin itself needs to be in the `buildscript`
block.  The database driver, parsers, and any other libraries needed to run Liquibase are now
specified as `liquibaseRuntime` dependencies in the `dependencies` block of your build file.  In
addition, the plugin no longer includes the Groovy DSL as a dependency.  If you want to use Groovy
for your changesets (and why wouldn't you?), the Groovy DSL will also need to be a
`liquibaseRuntime` dependency, and it will also need to be version `2.0.0` or later if you want to
use Liquibase versions > 3.4.2.

These changes make it easier to use new versions of Liquibase and the Groovy DSL as they come out
without having to override what the plugin itself is trying to do.  It also avoids the issues that
can happen when Liquibase wants different, and conflicting, libraries from what Gradle is using.

In addition to the changes to the way the plugin is configured, there are several other changes that
are worth noting:

1. There was a bug introduced in version 1.2.2 of the plugin regarding filenames and the
   `includeAll` change. Version 1.2.2 was incorrectly converting all changeset filenames to absolute
   paths, a bug that was fixed in version 2.0.0.  If you are updating from version 1.2.1 or earlier,
   this change should not affect you, but if you've run changes with version 1.2.2 through 1.2.4,
   you will need to fix some or all of the paths in the DATABASECHANGELOG table before running the
   2.0.0 version of the plugin.   Failing to do this wil result in Liquibase trying to run the
   changes again.

2. Liquibase made a change to the checksum logic in version 3.6.0.  According to the Liquibase
   documentation, Liquibase will just fix the checksums of each change when you run the first update
   command, but it won't detect changes to any changes that were marked with the `runOnChange`.  If
   you have any changes that use `runOnChange`, you should run an update once with your old version,
   then run it again with the new version to fix the checksums.

3. Liquibase changed the `resourceFilter` attribute of the `includeAll` element to just `filter`.
   Since the 2.0.0 version of the Groovy DSL was built for Liquibase 3.6.x, it will throw an error
   if it finds the old `resourceFilter` attribute, so you will need to convert any effected change
   sets.  Note that `includeAll` is one of the few things handled by the DSL itself, so `filter`
   will still work even if you're using an older version of Liquibase.

4. The `alterSequence` change used to have a `willCycle` attribute.  That attribute is now called
   `cycle`

5. Liquibase 3.6 appears to have broken console output and disabled the `--logLevel` argument.
   There is an issue in the Liquibase Jira 
   ([CORE-3220)](https://liquibase.jira.com/browse/CORE-3220)), but until it gets fixed, you can use
   a Proxy class in the plugin to enable console output. To use the proxy, simply add
   `mainClassName = 'org.liquibase.gradle.OutputEnablingLiquibaseRunner'` to your `liquibase` block
   in `build.gradle`.  This won't fix the problem with the logLevel argument, but you will at least
   be able to see output.

Usage
-----

The Liquibase plugin allows you to parse Liquibase changesets using any Liquibase parser that is in
the classpath when Liquibase runs.  Some parsers, such as the XML parser and the YAML parser, are
part of Liquibase itself, although some parsers require you to add additional dependencies to the 
liquibase classpath.  For example, the YAML parser requires `org.yaml:snakeyaml:1.17`.  Using this
plugin with Liquibase 4.4.0+ also requires the `info.picocli:picocli:4.6.1` library.

One of the best ways to parse Liquibase changesets is with the Groovy DSL, which is a much nicer way
to write changelogs, especially since Groovy is the language of Gradle scripts themselves.  The
Groovy DSL syntax intended to mirror the Liquibase XML syntax directly, such that mapping elements
and attributes from the Liquibase documentation to Groovy builder syntax will result in a valid
changelog. Hence, this DSL is not documented separately from the Liquibase XML format.  However, 
there are some minor differences or enhancements to the XML format, and there are some gaping holes
in Liquibase's documentation of the XML. Those holes are filled, and differences explained in the
documentation on the [Groovy Liquibase DSL](https://github.com/liquibase/liquibase-groovy-dsl) 
project page.  To use the Groovy DSL, simply include the Groovy DSL as a liquibaseRuntime dependency
and specify a `changeLogFile` that ends in .groovy.  For those who, for some reason, still prefer
XML, JSON, or Yaml, you can use these formats by specifying a `changeLogFile` that ends in the
appropriate extension, and Liquibase will find and use the correct parser.

The Liquibase plugin is meant to be a light-weight front end for the Liquibase command line utility.
When the liquibase plugin is applied, it creates a Gradle task for each command supported by
Liquibase. `gradle tasks` will list out these tasks.  The 
[Liquibase Documentation](http://www.liquibase.org/documentation/command_line.html) describes what
each command does and what parameters each command uses.  If you want to prefix each task to avoid
task name conflicts, set a value for the `liquibaseTaskPrefix` property.  This will tell the
liquibase plugin to capitalize the task name and prefix it with the given prefix.  For example, if
you put `liquibaseTaskPrefix=liquibase` in `gradle.properties`, then this plugin will create tasks
named `liquibaseUpdate`, `liquibaseTag`, etc.  You could do the same thing by adding the 
`-PliquibaseTaskPrefix=liquibase` argument when running Gradle, but using `gradle.properties` is
probably a better solution because all users would get the same tasks every time. 

The Liquibase plugin has some subtle differences from the Liquibase command line utility.

1. The liquibase `dbDoc` command has no default for the output directory, but the plugin does.  If
  no value is given to this command, the plugin will put it in `{buildDir}/database/docs`.

2. Some tasks like `updateSql` produce output.  Users of the plugin have 3 options for specifying
  how those tasks work
    - With no configuration, the output will simply go to STDOUT.
    - If the activity has an `outputFile` method, it will use that file for all tasks that support
      output files.
    - Users can specify `-PliquibaseOutputFile=myFile` to send output to a specific file.  If 
     specified, this command line option always wins.

3. The Liquibase `execute-sql` command works with either SQL strings or SQL files.  The `executeSql`
  task only supports SQL strings.  The plugin creates an `executeSqlFile` task for running SQL
  files.  Under the covers, they run the same `execute-sql` command.

There are 3 basic parts to using the Liquibase Gradle Plugin.  Including the plugin, setting up the
Liquibase runtime dependencies, and configuring the plugin.  Each step is described below.

#### 1. Including the plugin
To include the plugin into Gradle builds, simply add the following to your build.gradle file:

```groovy
plugins {
  id 'org.liquibase.gradle' version '2.2.0'
}
```

To use the older Gradle 2.0 style, add the following to build.gradle instead:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.liquibase:liquibase-gradle-plugin:2.2.0"
    }
}
apply plugin: 'org.liquibase.gradle'
```

#### 2. Setting up the classpath.
The plugin will need to be able to find Liquibase on the classpath when it runs a task, and
Liquibase will need to be able to find database drivers, changelog parsers, etc. in the classpath.
This is done by adding `liquibaseRuntime` dependencies to the `dependencies` block in the
`build.gradle` file.  At a minimum, you'll need to include Liquibase itself along with a database
driver.  Liquibase 4.4.0+ also requires the picocli library.  We also recommend including the
[Liquibase Groovy DSL](https://github.com/liquibase/liquibase-groovy-dsl) which parses changelogs
written in an elegant Groovy DSL instead of hurtful XML. An example of `liquibaseRuntime` entries is
below:

```groovy
dependencies {
  liquibaseRuntime 'org.liquibase:liquibase-core:4.16.1'
  liquibaseRuntime 'org.liquibase:liquibase-groovy-dsl:3.0.0'
  liquibaseRuntime 'info.picocli:picocli:4.6.1'
  liquibaseRuntime 'mysql:mysql-connector-java:5.1.34'
}
```

The `dependencies` block will contain many other dependencies to build and run your project, but
those dependencies are not part of the classpath when liquibase runs, because Liquibase typically
only needs to be able to parse the change logs and connect to the database, and I didn't want to
clutter up the classpath with dependencies that weren't needed.

Using this plugin with Java 9+ and XML based change sets will need to add JAXB th classpath since
JAXB was removed from the core JVM.  This can be done by adding the following to your
`liquibaseRuntime` dependencies:

```groovy
  liquibaseRuntime group: 'javax.xml.bind', name: 'jaxb-api', version: '2.3.1'
``` 

Some users have reported issues with logback and needed to add the following:

```groovy
  liquibaseRuntime("ch.qos.logback:logback-core:1.2.3")
  liquibaseRuntime("ch.qos.logback:logback-classic:1.2.3")
```

Users of the liquibase-hibernate module who need to run the Hibernate diff command, or generate a
changelog from Entity classes will need some extra configuration.  You'll need to add something like
the following to your `liquibaseRuntime` dependencies:

```groovy
  liquibaseRuntime 'org.liquibase.ext:liquibase-hibernate5:3.6' 
  liquibaseRuntime sourceSets.main.output
```

Adding `sourceSets.main.output` is necessary for Hibernate to find your entity classes.

If you have a lot of dependencies from your project that you need to have in the liquibase
classpath, you could also make `liquibaseRuntime` extend another configuration like this:

```groovy
configurations {
  liquibaseRuntime.extendsFrom runtime
}
```

Or, if you don't already have a `configurations` block, you can simply add
`configurations.liquibaseRuntime.extendsFrom configurations.runtime` to your build.gradle file.

#### 3. Configuring the plugin

Parameters for Liquibase commands are configured in the `liquibase` block inside the build.gradle
file.  This block contains a series of, "activities", each defining a series of Liquibase
parameters.  Any method in an "activity" is assumed to be a Liquibase command line parameter.  For
example, including `changelogFile 'myfile.groovy'` in an activity does the same thing as
`--changelog-file=myfile.groovy` would do on the command line.  Including `difftypes 'data'` in an
activity does the same thing as `difftypes=data` would do on the command line, etc.  The Liquibase
documentation details all the valid command line parameters. 

Some arguments changed in Liquibase 4.4, for example, `changeLogFile` became `changelogFile` with a
lowercase "l".  The plugin will automatically convert pre-4.4 names for a time to support backwards
compatibility.

The `liquibase` block also has an
optional "runList", which determines which activities are run for each task.  If no runList is
defined, the Liquibase Plugin will run all the activities.  NOTE: the order of execution when there
is no runList is not guaranteed.

*Example:*

Let's suppose that for each deployment, you need to update the data model for your application's
database, and you also need to run some SQL statements in a separate database used for security.  
Additionally, you want to occasionally run a diff between the changelog and the database.  The
 `liquibase` block might look like this:

```groovy
liquibase {
  activities {
    main {
      changelogFile 'src/main/db/main.groovy'
      url project.ext.mainUrl
      username project.ext.mainUsername
      password project.ext.mainPassword
    }
    security {
      changelogFile 'src/main/db/security.groovy'
      url project.ext.securityUrl
      username project.ext.securityUsername
      password project.ext.securityPassword
    }
    diffMain {
      changelogFile 'src/main/db/main.groovy'
      url project.ext.mainUrl
      username project.ext.mainUsername
      password project.ext.mainPassword
      difftypes 'data'
    }
  }
  runList = project.ext.runList
}
```

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

Some things to keep in mind when setting up the `liquibase` block:

1. We only need one activity block for each type of activity.  In the example above, the database
   credentials are driven by build properties so that the correct database can be specified at build
   time so that you don't need a separate activity for each database.

2. By making the value of `runList` a property, you can determine the activities that get run at
   build time.  For example, if you didn't need to run the security updates in the CI environment,
   you could type `gradle update -PrunList=main` For environments where you do need the security
   updates, you would use `gradle update -PrunList='main,security'`.  To do a diff, you'd run
   `gradle diff -PrunList=diffMain`.  This use of properties is the reason the runList is a string
   and not an array.

3. The methods in each activity block are meant to be pass-throughs to Liquibase.  Any valid
   Liquibase command parameter is a legal method here.  The command parameters are parameters in the
   Liquibase documentation that start with a `--` such as `--difftypes` or `--logLevel`.  For
   example, if you wanted to increase the log level, you could add `logLevel 'debug'` to the
   activity.  

4. In addition to the command pass-through methods of an activity, there is a `changeLogParameters`
   method.  This method takes a map, and is used to setup token substitution in the changeLogs.  See
   the Liquibase documentation for more details on token substitution.

5. Some Liquibase commands like `tag` and `rollback` require a value, in this case a tag name.  
   Since the value will likely change from run to run, the command value is not configured in the
   `liquibase` block.  To supply a command value, add `-PliquibaseCommandValue=<value>` to the
   gradle command.
   
6. Optionally, if you want to use a different entry point than the default
   `liquibase.integration.commandline.Main`, you can configure a different main class. This is
   useful if you want, for instance, to derive certain company-specific parameters.
   
```
liquibase {
  mainClassName = 'liquibase.ext.commandline.LiquibaseAlternativeMain'
}
```

For an example of how to configure and use this plugin, see the
[Liquibase Workshop](https://github.com/stevesaliman/liquibase-workshop) repo. That project contains
a `build.gradle` showing exactly how to configure the plugin, and an example directory setup as well.

Upgrading the version of Liquibase itself
-----------------------------------------
Most of the time, the new versions of Liquibase works the same as the old one, but sometimes the new
versions have compatibility issues with existing change sets, as happened when Liquibase released
version 3.  When this happens, we recommend the following procedure to do the upgrade:

1. Make sure all of your Liquibase managed databases are up-to-date by running `gradle update` on
   them *before upgrading to the new version of the Liquibase plugin*.

2. Create a new, throw away database to test your Liquibase change sets.  Run `gradle update` on the
   new database using the latest version of the Liquibase plugin.  This is important because of the
   deprecated items in the Groovy DSL, and because there are some subtle differences in the ways the
   different Liquibase versions generate SQL.  For example, adding a default value to a boolean
   column in MySql using `defaultValue: "0"` worked fine in Liquibase 2, but in Liquibase 3, it
   generates SQL that doesn't work for MySql - `defaultValueNumeric: 0` needs to be used instead.

3. Once you are sure all of your change sets work with the latest Liquibase plugin, clear all
   checksums that were calculated by the old version of Liquibase 2 by running 
   `gradle clearChecksums` against all databases.

4. Finally, run `gradle changelogSync` on all databases to calculate new checksums.

