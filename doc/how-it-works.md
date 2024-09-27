How it works
============

The Liquibase plugin is meant to be a light-weight front end for the Liquibase command line utility,
creating Gradle tasks for each Liquibase command, and passing arguments to Liquibase for execution
however they are specified in the build.

Tasks
-----

When this plugin is applied, it creates a Gradle task for each command supported by Liquibase.
`gradle tasks` will list out these tasks.  The
[Liquibase Documentation](http://www.liquibase.org/documentation/command_line.html) describes what
each command does and what parameters each command uses.  Note that while liquibase's documentation
uses kebab-case, the Gradle plugin uses camelCase because it is the Gradle/Groovy standard.

If you want to prefix each task to avoid task name conflicts, set a value for the
`liquibaseTaskPrefix` property.  This will tell the liquibase plugin to capitalize the task name and
prefix it with the given prefix.  For example, if you put `liquibaseTaskPrefix=liquibase` in
`gradle.properties`, then this plugin will create tasks named `liquibaseUpdate`, `liquibaseTag`,
etc.

The Liquibase plugin has some subtle differences from the Liquibase command line utility.

Arguments
---------

Arguments refer to Command Arguments and Global Arguments, as described in the Liquibase 
documentation.  As with tasks, this plugin supports the camelCase equivalents of Liquibase's
kebab-case arguments.

There are two ways to specify the arguments for liquibase commands:

1. In an `activity` block inside the `liquibase` block.  The [Usage](./usage.md) page goes into
  more detail, but basically, you can specify one or more "activities", each with its own collection
  of arguments, and the plugin will use those arguments when running that activity.  Any supported
  Liquibase argument can be placed in an `activity` block, and it will be passed to Liquibase.

2. Liquibase's arguments can be specified at run time by setting matching Gradle properties on the
  command line, prefixed by "liquibase".  For example, to run a Liquibase "update", specifying the
  database password on the command line, you could run `gradle update -PliquibasePassword=myPassword` 

At run time, the plugin will merge the arguments in the `activity` with recognized Gradle properties
from the command line.  Gradle's properties will override values in the activity block, providing
an easy way to override defaults if necessary.

Differences from the Liquibase Documentation
--------------------------------------------

As mentioned before, this plugin is meant to be a thin wrapper around the Liquibase CLI, but there
are a few subtle differences:

1. The liquibase `dbDoc` command has no default for the output directory, but the plugin does.  If
   no value is given to this command, the plugin will put it in `{buildDir}/database/docs`.

2. The value of the `logLevel` argument is "info".

3. Some tasks like `updateSql` produce output.  Users of the plugin have 3 options for specifying
   how those tasks work
    1. With no configuration, the output will simply go to STDOUT.
    2. If the activity has an `outputFile` method, it will use that file for all tasks that support
       output files.
    3. Users can specify `-PliquibaseOutputFile=myFile` to send output to a specific file.  If
       specified, this command line option always wins.

When running a Liquibase task, Liquibase will parse changesets using any Liquibase parser that is in
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
