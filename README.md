Liquibase Gradle Plugin
-----------------------
A plugin for [Gradle](http://gradle.org) that allows you to use 
[Liquibase](http://liquibase.org) to manage your database upgrades.  This 
project was created by Tim Berglund, and is currently maintained by Steve 
Saliman.

News
----
### September 3, 2018
Release 2.0.1 is a minor release that removes the CVE-2016-6814 vulnerability 
by updating the Groovy dependency.

### July 14, 2018
We're pleased to announce the release of version 2.0.0 of the Liquibase Gradle
plugin, with much thanks to Jasper de Vries (@litpho).  

**This has breaking changes** so please read all of the information in this 
section before upgrading.

Version 2.0.0 Changes the way the plugin sets up the classpath when running
Liquibase.  This allows us to isolate the classpath Liquibase uses from the one
Gradle is using.  Note that this is a breaking change!  Builds will not work 
without first fixing your build scripts to set up the classpath. 

Prior to version 2.0.0, you would need to include the plugin and the database
drivers in the `buildscript` block.  As of version 2.0.0, only the plugin itself
needs to be in the `buildscript` block.  The database driver, parsers, and any
other libraries needed to run Liquibase are now specified as `liquibaseRuntime`
dependencies in the `dependencies` block of your build file.  In addition, the
plugin no longer includes the Groovy DSL as a dependency.  If you want to use
Groovy for your changesets (and why wouldn't you?), the Groovy DSL will also
need to be a `liquibaseRuntime` dependency, and it will also need to be version
`2.0.0` or later if you want to use Liquibase versions > 3.4.2.

These changes make it easier to use new versions of Liquibase and the Groovy
DSL as they come out without having to override what the plugin itself is 
trying to do.  It also avoids the issues that can happen when Liquibase wants 
different, and conflicting, libraries from what Gradle is using.

In addition to the changes to to the way the plugin is configured, there are 
several other changes that are worth noting:

1. There was a bug introduced in version 1.2.2 of the plugin regarding filenames
   and the `includeAll` change. Version 1.2.2 was incorrectly converting all
   changeset filenames to absolute paths, a bug that was fixed in version 2.0.0.
   If you are updating from version 1.2.1 or earlier, this change should not
   effect you, but if you've run changes with version 1.2.2 through 1.2.4, you
   will need to fix some or all of the paths in the DATABASECHANGELOG table 
   before running the 2.0.0 version of the plugin.   Failing to do this wil
   result in Liquibase trying to run the changes again.

2. Liquibase made a change to the checksum logic in version 3.6.0.  According
   to the Liquibase documentation, Liquibase will just fix the checksums of each
   change when you run the first update command, but it won't detect changes to
   any changes that were marked with the `runOnChange`.  If you have any changes
   that use `runOnChange`, you should run an update once with your old version,
   then run it again with the new version to fix the checksums.

3. Liquibase changed the `resourceFilter` attribute of the `includeAll` element
   to just `filter`.  Since the 2.0.0 version of the Groovy DSL was built for 
   Liquibase 3.6.x, it will throw an error if it finds the old `resourceFilter`
   attribute, so you will need to convert any effected change sets.  Note that
   `includeAll` is one of the few things handled by the DSL itself, so `filter`
   will still work even if you're using an older version of Liquibase.

4. The `alterSequence` change used to have a `willCycle` attribute.  That
   attribute is now called `cycle`

5. Liquibase 3.6 appears to have broken console output and disabled the 
   `--logLevel` argument.  There is an issue in the Liquibase Jira 
   ([CORE-3220)](https://liquibase.jira.com/browse/CORE-3220)), but until it
   gets fixed, you can use a Proxy class in the plugin to enable console output.
   To use the proxy, simply add
   `mainClassName 'org.liquibase.gradle.OutputEnablingLiquibaseRunner'` to your 
   `liquibase` block in `build.gradle`.  This won't fix the problem with the 
   logLevel argument, but you will at least be able to see output.

### March 5, 2017
Version 1.2.4 is a minor release that fixes a bug with the excludeObjects and
includeObjects options.

### February 25, 2017
The previous release of this plugin had a broken dependency.  Version 1.2.3 
fixes this issue.  Please do not use version 1.2.2.

### February 20, 2017
The plugin has been updated to use the latest Groovy DSL, and I've worked 
around a Liquibase argument parsing bug.

### November 30, 2015
The plugin has been updated to support Liquibase 3.4.2.

Usage
-----
The Liquibase plugin allows you to parse Liquibase changesets using any 
Liquibase parser that is in the classpath when Liquibase runs.  Some parsers,
such as the XML parser and the YAML parser, are part of Liquiabse itself, 
although some parsers require you to add additional dependencies to the 
liquibase classpath.  For example, the YAML parser requires 
`org.yaml:snakeyaml:1.15`.

One of the best ways to parse Liquiabse changesets is with the Groovy DSL, 
which is a much nicer way to write changelogs, especially since Groovy is the 
language of Gradle scripts themselves.  The Groovy DSL syntax intended to 
mirror the Liquibase XML syntax directly, such that mapping elements and 
attributes from the Liquibase documentation to Groovy builder syntax will 
result in a valid changelog. Hence this DSL is not documented separately from
the Liquibase XML format.  However there are some minor differences or 
enhancements to the XML format, and there are some gaping holes in Liquibase's
documentation of the XML. Those holes are filled, and differences explained in
the documentation on the 
[Groovy Liquibase DSL](https://github.com/liquibase/liquibase-groovy-dsl) 
project page.  To use the Groovy DSL, simply include the Groovy DSL as a
liquibaseRuntime dependency and specify a `changeLogFile` that
ends in .groovy.  For those who, for some reason, still prefer XML, JSON, or
Yaml, you can use these formats by specifying a `changeLogFile` that ends in 
the appropriate extension, and Liquibase will find and use the correct parser.

The Liquibase plugin is meant to be a light weight front end for the Liquibase
command line utility.  When the liquibase plugin is applied, it creates a
Gradle task for each command supported by Liquibase. `gradle tasks` will
list out these tasks.  The
[Liquibase Documentation](http://www.liquibase.org/documentation/command_line.html)
describes what each command does and what parameters each command uses.  If you
want to prefix each task to avoid task name conflicts, set a value for the 
`liquibaseTaskPrefix` property.  This will tell the liquibase plugin to 
capitalize the task name and prefix it with the given prefix.  For example,
if Gradle is invoked with `-PliquibaseTaskPrefix=liquibase`, or you put
`liquibaseTaskPrefix=liquibase` in `gradle.properties` then this plugin will 
create tasks named `liquibaseUpdate`, `liquibaseTag`, etc.

There are 3 basic parts to using the Liquibase Gradle Plugin.  Including the
plugin, setting up the Liquibase runtime dependencies, and configuring the 
plugin.  Each step is described below.

#### 1. Including the plugin
To include the plugin into Gradle builds, simply add the following to your 
build.gradle file:

```groovy
plugins {
  id 'org.liquibase.gradle' version '2.0.1'
}
```

To use the older Gradle 2.0 style, add the following to build.gradle instead:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.liquibase:liquibase-gradle-plugin:2.0.1"
    }
}
apply plugin: 'org.liquibase.gradle'
```

#### 2. Setting up the classpath.
The plugin will need to be able to find Liquibase on the classpath when it runs
a task, and Liquibase will need to be able to find database drivers, changelog
parsers, etc. in the classpath.  This is done by adding `liquibaseRuntime` 
dependencies to the `dependencies` block in the `build.gradle` file.  At a
minimum, you'll need to include Liquibase itself along with a database driver.
We also recommend including the
[Liquibase Groovy DSL](https://github.com/liquibase/liquibase-groovy-dsl) 
which parses changelogs written in an elegant Groovy DSL instead of hurtful XML.
An example of `liquibaseRuntime` entries is below:

```groovy
dependencies {
  // All of your normal project dependencies would be here in addition to...
  liquibaseRuntime 'org.liquibase:liquibase-core:3.6.1'
  liquibaseRuntime 'org.liquibase:liquibase-groovy-dsl:2.0.1'
  liquibaseRuntime 'mysql:mysql-connector-java:5.1.34'
}
```

#### 3. Configuring the plugin

Parameters for Liquibase commands are configured in the `liquibase` block
inside the build.gradle file.  This block contains a series of, "activities", 
each defining a series of Liquibase parameters.  Any method in an "activity" is
assumed to be a Liquibase command line parameter.  For example, including
`changeLogFile 'myfile.groovy'` in an activity does the same thing as
`--changeLogfile=myfile.groovy` would do on the command line.  Including
`difftypes 'data'` in an activity does the same thing as `difftypes=data` would
do on the command line, etc.  The Liquibase documentation details all the valid
command line parameters.  The `liquibase` block also has an optional "runList",
which determines which activities are run for each task.  If no runList is
defined, the Liquibase Plugin will run all the activities.  NOTE: the order of
execution when there is no runList is not guaranteed.

*Example:*

Let's suppose that for each deployment, you need to update the data model for
your application's database, and wou also need to run some SQL statements
in a separate database used for security.  Additionally, you want to 
occasionally run a diff between the changelog and the database.  The
 `liquibase` block might look like this:

```groovy
liquibase {
  activities {
    main {
      changeLogFile 'src/main/db/main.groovy'
      url project.ext.mainUrl
      username project.ext.mainUsername
      password project.ext.mainPassword
    }
    security {
      changeLogFile 'src/main/db/security.groovy'
      url project.ext.securityUrl
      username project.ext.securityUsername
      password project.ext.securityPassword
    }
    diffMain {
      changeLogFile 'src/main/db/main.groovy'
      url project.ext.mainUrl
      username project.ext.mainUsername
      password project.ext.mainPassword
      difftypes 'data'
    }
  }
  runList = project.ext.runList
}
```

The `liquibase` block can also contain a `mainClassName` which tells the plugin
the name of the class to invoke in order to run Liquibase.  This value is 
optional and defaults to `liquibase.integration.commandline.Main`.  This value
can be changed to call other classes instead, such as the plugin's own 
`org.liquibase.gradle.OutputEnablingLiquibaseRunner` to fix a Liquibase 3.6
logging issue.

Some things to keep in mind when setting up the `liquibase` block:

1. We only need one activity block for each type of activity.  In the example 
   above, the database credentials are driven by build properties so that the
   correct database can be specified at build time so that you don't need a
   separate activity for each database.

2. By making the value of `runList` a property, you can determine the
   activities that get run at build time.  For example, if you didn't need to
   run the security updates in the CI environment, you could type
   `gradle update -PrunList=main` For environments where you do need the
   security updates, you would use `gradle update -PrunList='main,security'`.
   To do a diff, you'd run `gradle diff -PrunList=diffMain`.  This use of 
   properties is the reason the runList is a string and not an array.

3. The methods in each activity block are meant to be pass-throughs to 
   Liquibase.  Any valid Liquibase command parameter is a legal method here.
   The command parameters are parameters in the Liquibase documentation that
   start with a `--` such as `--difftypes` or `--logLevel`.  For example, if
   you wanted to increase the log level, you could add `logLevel debug` to the
   activity.  

4. In addition to the command pass-through methods of an activity, there is a
   `changeLogParameters` method.  This method takes a map, and is used to
   setup token substitution in the changeLogs.  See the Liquibase documentation
   for more details on token substitution.

5. Some Liquibase commands like `tag` and `rollback` require a value, in this
   case a tag name.  Since the value will likely change from run to run, the
   command value is not configured in the `liquibase` block.  To supply
   a command value, add `-PliquibaseCommandValue=<value>` to the gradle
   command.
   
6. Optionally, if you want to use a different entry point than the default
   `liquibase.integration.commandline.Main`, you can configure a different main
   class. This is useful if you want, for instance, to derive certain company-specific
   parameters.
```
liquibase {
  mainClassName = 'liquibase.ext.commandline.LiquibaseAlternativeMain'
}
```

For an example of how to configure and use this plugin, see the
[Liquibase Workshop](https://github.com/stevesaliman/liquibase-workshop) repo.
That project contains a `build.gradle` showing exactly how to configure the
plugin, and an example directory setup as well.

Upgrading the version of Liquibase itself
-----------------------------------------
Most of the time, the new versions of Liquibase works the same as the old one,
but sometimes the new versions have compatibility issues with existing change
sets, as happened when Liquibase 3 was released.  When this happens, we 
reccommend the following procedure to do the upgrade:

1. Make sure all of your Liquibase managed databases are up to date by running
   `gradle update` on them *before upgrading to the new version of the
   Liquibase plugin*.

2. Create a new, throw away database to test your Liquibase change sets.  Run
   `gradle update` on the new database using the latest version of the
   Liquibase plugin.  This is important because of the deprecated items in the
   Groovy DSL, and because there are some subtle differences in the ways the
   different Liquibase versions generate SQL.  For example, adding a default
   value to a boolean column in MySql using `defaultValue: "0"` worked fine
   in Liquibase 2, but in Liquibase 3, it generates SQL that doesn't work for
   MySql - `defaultValueNumeric: 0` needs to be used instead.

3. Once you are sure all of your change sets work with the latest Liquibase
   plugin, clear all checksums that were calculated by the old version of 
   Liquibase 2 by running `gradle clearChecksums` against all databases.

4. Finally, run `gradle changeLogSync` on all databases to calculate new
   checksums.

