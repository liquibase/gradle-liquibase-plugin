Releases
--------

**IMPORTANT:** As of version 2.2.0, this plugin no longer works with Gradle versions prior to 6.4.

**IMPORTANT:** Using version 2.1.0+ of this plugin with Liquibase 4.4.0+ requires additional 
configuration.  Liquibase now uses the picocli library to parse options, but for some reason that
library isn't a transitive dependency of Liquibase itself, so if you want to use this plugin with
Liquibase 4.4.0+, you'll have to add the `liquibaseRuntime 'info.picocli:picocli:4.6.1'` dependency
to your build.gradle file.

This page gives the highlights newer releases.  For complete details of each release, including 
older releases, see the [changelog](./changelog.md).

### Release 2.2.0 (March 4, 2023)

**Release 2.2.0 has some important and potentially breaking changes.**

- Gradle 8 is supported, versions prior to 6.4 are no longer supported.

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

- There is a new `-PliquibaseExtraArguments` property that can be used to override the arguments
  that the plugin sends to Liquibase.

### Release 2.1.1 (December 20, 2021)

Fixed the Code that detects the version of liquibase in use at the time the liquibase tasks run.

### Release 2.1.0 (November 13, 2021)

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

