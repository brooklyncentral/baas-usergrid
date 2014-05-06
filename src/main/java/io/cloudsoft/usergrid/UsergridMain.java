package io.cloudsoft.usergrid;

import io.airlift.command.Command;
import io.airlift.command.Option;
import io.cloudsoft.usergrid.app.UsergridBasicApp;
import io.cloudsoft.usergrid.app.UsergridClusteredApp;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects.ToStringHelper;

import brooklyn.cli.Main;

/**
 * This class provides a static main entry point for launching a custom Brooklyn-based app.
 * <p>
 * It inherits the standard Brooklyn CLI options from {@link Main}, plus adds a few more shortcuts for
 * favourite blueprints to the {@link LaunchCommand}.
 */
public class UsergridMain extends Main {

    private static final Logger log = LoggerFactory.getLogger(UsergridMain.class);

    public static final String DEFAULT_LOCATION = "localhost";

    public static void main(String... args) {
        log.debug("CLI invoked with args " + Arrays.asList(args));
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
        @Option(name = {"--single"}, description = "Launch a single cassandra/tomcat instance")
        public boolean single;

        @Option(name = {"--cluster"}, description = "Launch a clustered cassandra/tomcat deployment")
        public boolean cluster;

        @Override
        public Void call() throws Exception {
            // process our CLI arguments
            if (single) {
                setAppToLaunch(UsergridBasicApp.class.getCanonicalName());
            }
            if (cluster) {
                setAppToLaunch(UsergridClusteredApp.class.getCanonicalName());
            }

            // now process the standard launch arguments
            return super.call();
        }

        @Override
        public ToStringHelper string() {
            return super.string()
                .add("single", single)
                .add("cluster", cluster);
        }
    }
}
