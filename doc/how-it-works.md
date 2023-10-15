# How it works

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
