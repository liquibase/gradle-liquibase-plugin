Upgrading the version of Liquibase itself
-----------------------------------------

Most of the time, new versions of Liquibase work the same as old ones, but sometimes a new version
will have compatibility issues with existing change sets, as happened when Liquibase released
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
