package io.cloudsoft.usergrid;

import io.airlift.command.Command;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.cli.Main;

/**
 * This class provides a static main entry point for launching a custom Brooklyn-based app.
 * <p>
 * It inherits the standard Brooklyn CLI options from {@link Main},
 * plus adds a few more shortcuts for favourite blueprints to the {@link LaunchCommand}.
 */
public class UsergridMain extends Main {
    
    private static final Logger log = LoggerFactory.getLogger(UsergridMain.class);
    
    public static final String DEFAULT_LOCATION = "localhost";

    public static void main(String... args) {
        log.debug("CLI invoked with args "+Arrays.asList(args));
        new UsergridMain().execCli(args);
    }

    @Override
    protected String cliScriptName() {
        return "start.sh";
    }
    
    @Override
    protected Class<? extends BrooklynCommand> cliLaunchCommand() {
        return LaunchCommand.class;
    }

    @Command(name = "launch", description = "Starts a brooklyn server")
    public static class LaunchCommand extends Main.LaunchCommand {

    }
}
