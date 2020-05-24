Changes for 2.0.3
=================
- Fixed a problem caused by changes in Gradle 6.4 (Issue #70), with thanks to
  Patrick Haun (@bomgar). 
  
- Fixed a deprecation warning that started showing up in Gradle 6.0.
  
Changes for 2.0.2
=================
- Fixed the way the plugin handles System properties.  Liquibase will now 
  inherit System properties from the parent JVM when it runs, so you can now
  define System properties when you invoke Gradle, or in your build.gradle 
  file, and Liquibase will use them.  This fixes a problem with overriding the 
  change log table that Liquibase uses.

- Fixed a bug that was preventing some command arguments from being processed
  correctly by Liquibase.  Specifically, I improved the list of arguments that
  need to come *after* the command (Issue #64).
 
- Updated the Gradle Wrapper to use Gradle 6.
  
Changes for 2.0.1
=================
- Updated the version of Groovy to 2.4.12 to remove the CVE-2016-6814
  vulnerability

Changes for 2.0.0
=================
- The plugin no longer has a transitive dependency on the Liquibase Groovy DSL.
  **THIS IS A BREAKING CHANGE!** The Groovy DSL is what brought in Liquibase 
  itself.  It is now up to you to make sure the Groovy DSL and Liquibase itself
  are on the classpath via `liquibaseRuntime` dependencies. This resolves 
  Issue 11, Issue 29, and Issue 36.  Thank you to Jasper de Vries (@litpho) for
  his contribution to this release.

Changes for 1.2.4
=================
- fixed support for the excludeObjects/includeObjects options with thanks to
  @manuelsanchezortiz (Issue 23).
  
Changes for 1.2.3
=================
- Updated the plugin to use the correct, non-snapshot release of the Groovy DSL
  (Issue #22).
  
Changes for 1.2.2
=================
- Updated the plugin to use the latest Groovy DSL bug fixes

- Worked around a Liquibase bug that was causing problems with the ```status```
  command (Issue #3).
  
Changes for 1.2.1
=================
- Updated the DSL to fix a customChange issue.

Changes for 1.2.0
=================
- Updated the DSL to support most of Liquibase 3.4.2 (Issue #4 and Issue #6)

Changes for 1.1.1
=================
- Added support for Liquibase 3.3.5

- Fixed the task descriptions to correctly identify the property that is used
  to pass values to commands (Issue #2)
  
Changes for 1.1.0
=================
- Refactored the project to fit into the Liquibase organization.

Changes for 1.0.2
=================
- Bumped the dependency on the Groovy DSL to a version that works with Java
  versions before JKD8 (Issue #27)

Changes for 1.0.1
=================
- Added support for prefixes for liquibase task names (Issue #20)

- Added support for Liquibase 3.3.2.

- Fixed the ```status``` and ```unexpectedChangeSets``` commands to support the
  ```--verbose``` command value.
