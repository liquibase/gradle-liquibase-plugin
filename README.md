Liquibase Gradle Plugin
-----------------------

A plugin for [Gradle](http://gradle.org) that allows you to use [Liquibase](http://liquibase.org)
to manage your database upgrades.  This project was originally created by Tim Berglund, and is
currently maintained by Steve Saliman.

Release 3.0.1 has been released
-------------------------------

Release 3.0.1 fixes a classpath issue that caused the plugin to call Liquibase incorrectly.

After far too long, release 3.0.0 fixes compatability issues with newer versions of Liquibase. 
**THIS IS A BREAKING CHANGE!**.  The plugin no longer works with versions of Liquibase older than
4.24.

Users updating from prior versions of this plugin should look at the [Releases](./doc/releases.md)
page for more information about the releases, including any breaking changes.

Documentation
-------------

- [Releases](./doc/releases.md)
- [How it works](./doc/how-it-works.md)
- [Usage](./doc/usage.md)
- [Upgrading Liquibase](./doc/upgrading-liquibase.md)

