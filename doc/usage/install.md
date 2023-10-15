# Including the plugin

To include the plugin into Gradle builds, simply add the following to your build.gradle file:

## Groovy DSL

```groovy
plugins {
  id 'org.liquibase.gradle' version '2.2.0'
}
```

To use the older Gradle 2.0 style, add the following to build.gradle instead:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.liquibase:liquibase-gradle-plugin:2.2.0"
    }
}
apply plugin: 'org.liquibase.gradle'
```

## Kotlin DSL

To be updated.