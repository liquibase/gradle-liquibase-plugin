## 2. Setting up the classpath.

The plugin will need to be able to find Liquibase on the classpath when it runs a task, and
Liquibase will need to be able to find database drivers, changelog parsers, etc. in the classpath.
This is done by adding `liquibaseRuntime` dependencies to the `dependencies` block in the
`build.gradle` file.  At a minimum, you'll need to include Liquibase itself along with a database
driver.  Liquibase 4.4.0+ also requires the picocli library. 
