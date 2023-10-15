# Groovy DSL


## Setting up the classpath

Example

```groovy
dependencies {
  liquibaseRuntime 'org.liquibase:liquibase-core:4.16.1'
  liquibaseRuntime 'org.liquibase:liquibase-groovy-dsl:3.0.2'
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

### 3. Configuring the plugin
 For example:

```groovy
liquibase {
  jvmArgs "-Duser.dir=$project.projectDir" 
}
```

*Example1:*

A simple example might look like this:

```groovy
liquibase {
  activities {
    main {
      changelogFile 'src/main/db/main.groovy'
      url project.ext.mainUrl
      username project.ext.mainUsername
      password project.ext.mainPassword
      logLevel "info"
    }
  }
}
```

This example will work for many, if not most projects.  It defines the parameters that all commands
will need, such as username and password, and there is only one activity.

*Example2:*

The plugin allows you to be much more complex if your situation requires it.

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
      logLevel "info"
    }
    security {
      changelogFile 'src/main/db/security.groovy'
      url project.ext.securityUrl
      username project.ext.securityUsername
      password project.ext.securityPassword
      logLevel "info"
    }
    diffMain {
      changelogFile 'src/main/db/main.groovy'
      url project.ext.mainUrl
      username project.ext.mainUsername
      password project.ext.mainPassword
      difftypes 'data'
      logLevel "info"
    }
  }
  runList = project.ext.runList
}
```

There are a few things to keep in mind when setting up the `liquibase` block:

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
   method.  This method takes a map, and is used to set up token substitution in the changeLogs.  See
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
  mainClassName 'liquibase.ext.commandline.LiquibaseAlternativeMain'
}
```

For an example of how to configure and use this plugin, see the
[Liquibase Workshop](https://github.com/stevesaliman/liquibase-workshop) repo. That project contains
a `build.gradle` showing exactly how to configure the plugin, and an example directory setup as well.