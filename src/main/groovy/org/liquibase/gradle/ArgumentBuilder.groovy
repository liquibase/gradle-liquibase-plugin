package org.liquibase.gradle


import liquibase.command.CommandArgumentDefinition
import liquibase.command.CommandDefinition
import liquibase.configuration.ConfigurationDefinition
import liquibase.configuration.LiquibaseConfiguration
import liquibase.Scope

/**
 * This class builds the Liquibase argument array for liquibase 4.24+.  Starting with Liquibase 4.4,
 * Liquibase no longer silently ignores Command Arguments that are not supported by the command, so
 * we need to be particular about how we put together the array.
 * <p>
 * This class puts together arguments in the following order:
 * <ol>
 * <li>Global Arguments, such as --classpath or --log-level.</li>
 * <li>The Command itself.</li>
 * <li>Command Arguments --username or --changelog-file.</li>
 * <li>Changelog parameters, which are supplied as "-D" arguments.  This only happens if we are also
 * sending the changelog-file parameter.</li>
 * <p>
 * There is a relationship in this class between Liquibase arguments and properties.  Properties are
 * the Gradle property representation of a Liquibase argument.  For example, if Liquibase has an
 * argument named "changelogFile", users can define "-PLiquibaseChangelogFile" to pass an argument
 * at runtime.
 *
 * @author Steven C. Saliman
 */
class ArgumentBuilder {
    // All known Liquibase global arguments.
    static Set<String> allGlobalArguments
    // All Known Liquibase global arguments, as they can be passed in as properties.
    static Set<String> allGlobalProperties
    // All known Liquibase command arguments.
    static Set<String> allCommandArguments
    // All known Liquibase command arguments, as they can be passed in as properties.
    static Set<String> allCommandProperties
    // All known Liquibase commands.
    static Set<String> allCommands = new HashSet<>()

    // The Gradle project, used for logging.
    def project

    /**
     * Add a Liquibase command to our collection of known commands.  The plugin adds them one at a
     * time as it creates tasks.  The Argument Builder will then use this list to figure out what
     * command arguments are supported.  We won't take the time to figure out supported arguments
     * unless we're actually running a liquibase task.
     *
     * @param liquibaseCommand the command to add.
     */
    def addCommand(CommandDefinition liquibaseCommand) {
        allCommands += liquibaseCommand
    }

    /**
     * Build arguments, in the right order, to pass to Liquibase.
     *
     * @param activity the activity being run, which contains global and command parameters.
     * @param liquibaseCommand the liquibase being run.  This will be used to determine which
     *         command arguments are valid for the set of arguments we're building.
     * @return the argument string to pass to liquibase when we invoke it.
     */
    def buildLiquibaseArgs(Activity activity, CommandDefinition liquibaseCommand) {
        // Start by initializing our static option and property sets.
        initializeCommandArguments()
        initializeGlobalArguments()

        // This is what we'll ultimately return.
        def liquibaseArgs = []

        // Different parts of our liquibaseArgs before we string 'em all together.
        def globalArgs = []
        def commandArguments = []
        def supportedCommandArguments = []
        def sendingChangelog = false

        // Build a list of all the arguments (and argument aliases) supported by the given command.
        liquibaseCommand.getArguments().each { argName, a ->
            supportedCommandArguments += a.name
            // Starting with Liquibase 4.16, command arguments can have aliases
            def supportsAliases = CommandArgumentDefinition.getDeclaredFields().find { it.name == "aliases" }
            if ( supportsAliases ) {
                supportedCommandArguments += a.aliases
            }
        }

        globalArgs += argumentString("integrationName", "gradle")

        // Create a merged map of activity arguments and arguments given as Gradle properties, then
        // process each of the arguments from the map, figuring out what kind of argument each one
        // is and responding accordingly.
        createArgumentMap(activity.arguments, project).each {
            def argumentName = it.key
            if ( allGlobalArguments.contains(argumentName) ) {
                // We're dealing with global arg.
                globalArgs += argumentString(argumentName, it.value)
            } else if ( supportedCommandArguments.contains(argumentName) ) {
                // We have a command argument, and it is supported by this command.
                // Liquibase 4.4+ has a bug with the way it handles CLI defined changelog
                // parameters with the drop-all command.  It fails to send them to the changelog
                // parser, causing the parse to fail when changelogs use parameters.
                // https://github.com/liquibase/liquibase/issues/3380  As a workaround, we won't
                // add the argument if it is the changeLogFile arg, and we're running the
                // drop-all command.
                if ( argumentName == 'changelogFile' ) {
                    if ( liquibaseCommand.name[0] == 'drop-all' ) {
                        return
                    }
                    // Still here?  It's changelogFile, but not drop-all.  Note that we will be
                    // sending a Changelog
                    sendingChangelog = true
                }
                commandArguments += argumentString(argumentName, it.value)
            } else {
                // If nothing matched above, then we had a command argument that was not supported
                // by the command being run.
                project.logger.debug("skipping the ${argumentName} command argument because it is not supported by the ${liquibaseCommand.name[0]}")
            }
        }

        // If we're processing the db-doc command, and we don't have an output directory in our
        // command arguments, add it here.  The db-doc command is the only one that has a default
        // value.
        if ( liquibaseCommand.name[0] == "dbDoc" && !commandArguments.any {it.startsWith("--output-directory") } ) {
            commandArguments += "--output-directory=${project.buildDir}/database/docs"
        }

        // Now build our final argument array in the following order:
        // global args, command, command args, changelog parameters (-D args)
        liquibaseArgs = globalArgs + toKebab(liquibaseCommand.name[0]) + commandArguments

        // If we're sending a changelog, we need to also send change log parameters.  Unfortunately,
        // due to a bug in liquibase itself (https://liquibase.jira.com/browse/CORE-2519), we need
        // to put the -D arguments after the command.  If we put them before the command, they are
        // ignored
        if ( sendingChangelog ) {
            def changelogParamMap = createChangelogParamMap(activity)
            changelogParamMap.each { liquibaseArgs += "-D${it.key}=${it.value}" }

        }

        return liquibaseArgs
    }

    /**
     * Initialize the command argument and command properties sets, if we haven't done it already.
     * We do this only once for performance reasons.
     * <p>
     * This method loops through all the Liquibase commands, asking each for its supported
     * arguments.  Each one is added to the argument array as-is, and to the property set after
     * capitalizing it and adding "liquibase" to the front.
     */
    private initializeCommandArguments() {
        // Have we done this already?
        if ( allCommandArguments ) {
            return
        }

        allCommandArguments = new HashSet<>()
        allCommandProperties = new HashSet<>()

        allCommands.each { command ->
            // Add this command's supported arguments to the set of overall command arguments.
            command.getArguments().each { argName, a ->
                // We'll deal with changelogParameters in a special way later.
                if ( argName == "changelogParameters" ) {
                    return
                }
                allCommandArguments += argName
                allCommandProperties += "liquibase" + argName.capitalize()
                def supportsAliases = CommandArgumentDefinition.getDeclaredFields().find { it.name == "aliases" }
                if ( supportsAliases ) {
                    a.aliases.each { aliasName ->
                        allCommandArguments += aliasName
                        allCommandProperties += "liquibase" + aliasName.capitalize()
                    }
                }
            }
        }
    }

    /**
     * Initialize the global argument and global properties sets, if we haven't done it already.
     * We do this only once for performance reasons.
     * <p>
     * This method asks Liquibase for all the supported global arguments.  Each one is added to the
     * argument array as-is, and to the property set after capitalizing it and adding "liquibase"
     * to the front.
     */
    private initializeGlobalArguments() {
        // Have we done this already?
        if ( allGlobalArguments ) {
            return
        }

        allGlobalArguments = new HashSet<>()
        allGlobalProperties = new HashSet<>()
        // This is also how LiquibaseCommandLine.addGlobalArgs() gets global args.
        SortedSet<ConfigurationDefinition<?>> globalConfigurations = Scope
                .getCurrentScope()
                .getSingleton(LiquibaseConfiguration.class)
                .getRegisteredDefinitions(false);
        globalConfigurations.each { opt ->
            // fix it and add it.
            def fixedArg = fixGlobalArgument(opt.getKey())
            allGlobalArguments += fixedArg
            allGlobalProperties += "liquibase" + fixedArg.capitalize()
            opt.getAliasKeys().each {
                def fixedAlias = fixGlobalArgument(it)
                allGlobalArguments += fixedAlias
                allGlobalProperties += "liquibase" + fixedAlias.capitalize()
            }
        }
    }

    /**
     * Little helper method to "fix" a global argument.  Many of the argument names, as Liquibase
     * gives them to us, start with "liquibase.".  We want to remove that prefix.  We also want to
     * remove dots and change what follows a dot to be capitalized.  For example, "sql.showSql"
     * will become "sqlShowSql".
     *
     * @param arg the argument to fix.
     * @return the fixed arg.
     */
    private fixGlobalArgument(arg) {
        return (arg - "liquibase.").replaceAll("(\\.)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() })
    }

    /**
     * Helper method to create an argument map that combines the activity's arguments and the
     * arguments passed in via supported {@code liquibase} properties from the Gradle command line.
     * <p>
     * The output of this method is a map of argument names and their values.  The Gradle properties
     * will be processed after the activity arguments so that they override whatever was in the
     * activity.
     * <p>
     * When this method processes Gradle properties, it filters out properties Liquibase doesn't
     * recognize.  It does this silently because not all properties that start with "liquibase" is
     * meant to be an argument.  For example, "liquibaseVersion" is a common property to define the
     * version Gradle should use in the build, but it is not meant to be passed on to Liquibase
     * itself.
     *
     * @param arguments the arguments from the activity
     * @param project the project, from which we'll get the extra arguments.
     * @return a map of argument names and their values.
     */
    private createArgumentMap(arguments, project) {
        def argumentMap = [:]
        // Start with the activity's arguments
        arguments.each {
            // We'll handle changelog parameters later.
            if ( it.key != "changelogParameters" ) {
                project.logger.trace("liquibase-plugin:    Setting ${it.key}=${it.value} from activities")
                argumentMap.put(it.key, it.value)
            }
        }

        // Now go through all of the project properties that start with "liquibase" and use them
        // to override/add to the arguments, ignoring the ones Liquibase won't recognize.
        project.properties.findAll {
            if ( !allGlobalProperties.contains(it.key) && !allCommandProperties.contains(it.key) ) {
                return false
            }

            // Tasks are also properties, and there is a liquibaseTag task that we want to ignore.
            if ( LiquibaseTask.class.isAssignableFrom(it.value.class) ) {
                return false
            }
            return true
        }.each {
            def argName = it.key - "liquibase"
            argName = argName.uncapitalize()
            project.logger.trace("liquibase-plugin:    Setting ${argName}=${it.value} from the command line")
            argumentMap.put(argName, it.value)
        }

        // Return the sorted map.  Unit tests need to have a predictable argument order, and
        // Liquibase doesn't care about order, just what is before and after the command.
        return argumentMap.sort()
    }

    /**
     * Helper method to create a Changelog Parameter map that combines the activity's changelog
     * parameters and the parameters passed in via the {@code liquibaseChangelogParameters}
     * property from the Gradle command line.
     * <p>
     * The output of this method is a map of parameter names and their values.  The Gradle
     * properties will be processed after the activity arguments so that they override whatever was
     * in the activity.
     *
     * @param arguments the arguments from the activity
     * @param project the project, from which we'll get the extra arguments.
     * @return a map of argument names and their values.
     */
    private createChangelogParamMap(activity) {
        def changelogParameters = [:]

        // Start by adding parameters from the activity
        activity.changelogParameters.each {
            project.logger.trace("liquibase-plugin:    Adding activity changelogParameter ${it.key}=${it.value}")
            changelogParameters.put(it.key, it.value)
        }

        // Override/add to the map with project properties
        if ( !project.hasProperty("liquibaseChangelogParameters") ) {
            return changelogParameters
        }
        project.properties.get("liquibaseChangelogParameters").split(",").each {
            def (key, value) = it.split("=")
            project.logger.trace("liquibase-plugin:    Adding property changelogParameter ${key}=${value}")
            changelogParameters.put(key, value)
        }
        return changelogParameters
    }

    /**
     * Determine the correct argument string to send to Liquibase.  The argument name will be
     * converted to kebab-case, and we'll add the value if we have one.  If we don't we'll assume
     * we are dealing with a boolean argument.
     *
     * @param argumentName the name of the argument to process
     * @param the value of the argument to process.  If this is null, this method assumes we're
     *         dealing with a boolean argument.
     * @return the argument string to send to Liquibase.
     */
    private argumentString(argumentName, argumentValue) {
        // convert to kebab case.
        def option = toKebab(argumentName)

        // return the right argument string.  If we don't have a value, assume a boolean argument.
        return argumentValue ? "--${option}=${argumentValue}" : "--${option}"
    }

    /**
     * Helper method to convert a string to kebab-case.
     * @param str the string to convert
     * @return the converted string.
     */
    private toKebab(str) {
        return str.replaceAll("([A-Z])", { Object[] it -> "-" + it[1].toLowerCase() })
    }

}
