Usage
-----

There are 3 basic parts to using the Liquibase Gradle Plugin. 

1. [Including the plugin](#1-including-the-plugin)
2. [Setting up the Liquibase runtime dependencies](#2-setting-up-the-classpath)
3. [Configuring the plugin](#3-configuring-the-plugin)

### 1. Including the plugin


<details open>
<summary><b>Groovy</b></summary>

To include the plugin into Gradle builds, simply add the following to your build.gradle file:

```groovy
plugins {
  id 'org.liquibase.gradle' version '2.2.1'
}
```

To use the older Gradle 2.0 style, add the following to build.gradle instead:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.liquibase:liquibase-gradle-plugin:2.2.1"
    }
}
apply plugin: 'org.liquibase.gradle'
```

</details>
<details>
<summary><b>Kotlin</b></summary>

To include the plugin into Gradle builds, simply add the following to your build.gradle.kts file:

Coming Soon
</details>

### 2. Setting up the classpath

The plugin will need to be able to find Liquibase on the classpath when it runs a task, and
Liquibase will need to be able to find database drivers, changelog parsers, etc. in the classpath.
This is done by adding `liquibaseRuntime` dependencies to the `dependencies` block in the
`build.gradle` file.  At a minimum, you'll need to include Liquibase itself along with a database
driver.  Liquibase 4.4.0+ also requires the picocli library.  We also recommend including the
[Liquibase Groovy DSL](https://github.com/liquibase/liquibase-groovy-dsl) which parses changelogs
written in an elegant Groovy DSL instead of hurtful XML. An example of `liquibaseRuntime` entries is
below:

<details open>
<summary><b>Groovy</b></summary>

```groovy
dependencies {
  liquibaseRuntime 'org.liquibase:liquibase-core:4.16.1'
  liquibaseRuntime 'org.liquibase:liquibase-groovy-dsl:3.0.2'
  liquibaseRuntime 'info.picocli:picocli:4.6.1'
  liquibaseRuntime 'mysql:mysql-connector-java:5.1.34'
}
```

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>

The `dependencies` block will contain many other dependencies to build and run your project, but
those dependencies are not part of the classpath when liquibase runs, because Liquibase typically
only needs to be able to parse the change logs and connect to the database, and I didn't want to
clutter up the classpath with dependencies that weren't needed.

Using this plugin with Java 9+ and XML based change sets will need to add JAXB th classpath since
JAXB was removed from the core JVM.  This can be done by adding the following to your
`liquibaseRuntime` dependencies:

<details open>
<summary><b>Groovy</b></summary>

```groovy
  liquibaseRuntime group: 'javax.xml.bind', name: 'jaxb-api', version: '2.3.1'
``` 

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>

Some users have reported issues with logback and needed to add the following:

<details open>
<summary><b>Groovy</b></summary>

```groovy
  liquibaseRuntime("ch.qos.logback:logback-core:1.2.3")
  liquibaseRuntime("ch.qos.logback:logback-classic:1.2.3")
```

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>

Users of the liquibase-hibernate module who need to run the Hibernate diff command, or generate a
changelog from Entity classes will need some extra configuration.  You'll need to add something like
the following to your `liquibaseRuntime` dependencies:

<details open>
<summary><b>Groovy</b></summary>

```groovy
  liquibaseRuntime 'org.liquibase.ext:liquibase-hibernate5:3.6' 
  liquibaseRuntime sourceSets.main.output
```

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>

Adding `sourceSets.main.output` is necessary for Hibernate to find your entity classes.

If you have a lot of dependencies from your project that you need to have in the liquibase
classpath, you could also make `liquibaseRuntime` extend another configuration like this:

<details open>
<summary><b>Groovy</b></summary>

```groovy
configurations {
  liquibaseRuntime.extendsFrom runtime
}
```

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>

Or, if you don't already have a `configurations` block, you can simply add
`configurations.liquibaseRuntime.extendsFrom configurations.runtime` to your build.gradle file.


### 3. Configuring the plugin

Configuring the plugin involves understanding three basic concepts: commands, parameters, and
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

- Activities are introduced by the plugin.  A command is a specific Liquibase task to be done, while
  an activity is a more high level, broad category, and can be used to run the same command in
  different ways.  For example, deploying an application might involve updating the application's
  schema, but it might also involve inserting metadata about the application in a registry in a 
  different database.  The plugin achieves this by allowing users to define 2 activities, each
  referring to different databases and changelogs.  Activities can also be thought of as a
  collection of parameters that need to be grouped together.

With these concepts in mind, parameters for Liquibase commands are configured in the
`liquibase` block inside the build.gradle file, using one or more "activities", each defining a
series of Liquibase parameters.  Any method in an "activity" is assumed to be a Liquibase command
line parameter.  For example, including `changelogFile 'myfile.groovy'` in an activity does the same
thing as `--changelog-file=myfile.groovy` would do in the Liquibase CLI.  Including
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

The arguments in `build.gradle` can be overridden on the command line using the `liquibaseExtraArgs`
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
need to override the `user.dir` using the `jvmArgs`. For example:

<details open>
<summary><b>Groovy</b></summary>


```groovy
liquibase {
  jvmArgs "-Duser.dir=$project.projectDir" 
}
```

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>

See the [Examples](./examples.md) page for more details and examples of various configurations