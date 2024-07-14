Examples
--------

There are a few things to keep in mind when setting up the `liquibase` block:

1. We only need one activity block for each type of activity.  In the example below, the database
   credentials are driven by build properties so that the correct database can be specified at build
   time so that you don't need a separate activity for each database.

2. By making the value of `runList` a property, you can determine the activities that get run at
   build time.  For example, if you didn't need to run the security updates in the CI environment,
   you could type `gradle update -PrunList=main` For environments where you do need the security
   updates, you would use `gradle update -PrunList='main,security'`.  To do a diff, you'd run
   `gradle diff -PrunList=diffMain`.  This use of properties is the reason the runList is a string
   and not an array.

3. The methods in each activity block are meant to be pass-throughs to Liquibase.  Any valid
   Liquibase command parameter or global parameter is a legal method here.  The parameters are
   parameters in the Liquibase documentation that start with a `--` such as `--difftypes` or
   `--log-level`.  For example, if you wanted to increase the log level, you could add
   `logLevel 'debug'` to the activity.  Remember that kebab-case arguments from the documentation
   become camelCase in the plugin.

4. In addition to the command pass-through methods of an activity, there is a `changelogParameters`
   method.  This method takes a map, and is used to set up token substitution in the changeLogs. 
   See the Liquibase documentation for more details on token substitution.

Here are a few examples of `liquibase` blocks in the build.gradle file.

- [Example 1](#example1): The simple example.
- [Example 2](#example2): Multiple activities.

### Example1

A simple example might look like this:

This example will work for many, if not most projects.  It defines the parameters that all commands
will need, such as username and password, and there is only one activity.

<details open>
<summary><b>Groovy</b></summary>

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

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>


### Example2

The plugin allows you to be much more complex if your situation requires it.

Let's suppose that for each deployment, you need to update the data model for your application's
database, and you also need to run some SQL statements in a separate database used for security.  
Additionally, you want to occasionally run a diff between the changelog and the database.  The
`liquibase` block might look like this:

<details open>
<summary><b>Groovy</b></summary>

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

</details>
<details>
<summary><b>Kotlin</b></summary>

Coming Soon
</details>


If you want to use a different entry point than the default 
`liquibase.integration.commandline.LiquibaseCommandLine`, you can configure a different main class.
This is useful if you want, for instance, to derive certain company-specific parameters.

```groovy
liquibase {
  mainClassName 'liquibase.ext.commandline.LiquibaseAlternativeMain'
}
```

For an example of how to configure and use this plugin, see the
[Liquibase Workshop](https://github.com/stevesaliman/liquibase-workshop) repo. That project contains
a `build.gradle` showing exactly how to configure the plugin, and an example directory setup as well.
