package io.cloudsoft.usergrid.app;

import io.brooklyn.camp.brooklyn.BrooklynCampPlatform;
import io.brooklyn.camp.brooklyn.BrooklynCampPlatformLauncherNoServer;
import io.brooklyn.camp.spi.Assembly;
import io.brooklyn.camp.spi.AssemblyTemplate;

import java.io.File;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import brooklyn.entity.Application;
import brooklyn.entity.Entity;
import brooklyn.entity.basic.BrooklynTaskTags;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.EntityPredicates;
import brooklyn.entity.rebind.RebindTestUtils;
import brooklyn.entity.rebind.persister.PersistMode;
import brooklyn.launcher.BrooklynLauncher;
import brooklyn.management.ManagementContext;
import brooklyn.management.Task;
import brooklyn.management.internal.LocalManagementContext;
import brooklyn.management.internal.ManagementContextInternal;
import brooklyn.util.ResourceUtils;
import brooklyn.util.os.Os;
import brooklyn.util.stream.Streams;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

public abstract class AbstractYamlTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractYamlTest.class);

    protected static final String DEFUALT_LIVE_LOCATION = "aws-sles";
    protected static final String LOCALHOST_LOCATION = "localhost";

    private File persistenceDir;
    private BrooklynLauncher brooklynLauncher;
    private ManagementContext brooklynMgmt;
    private BrooklynCampPlatform platform;
    private BrooklynCampPlatformLauncherNoServer launcher;

    public AbstractYamlTest() {
        super();
    }

    @BeforeMethod(alwaysRun = true)
    public void setup() throws Exception {
        persistenceDir = Files.createTempDir();
        brooklynLauncher = BrooklynLauncher.newInstance()
                .persistMode(PersistMode.CLEAN)
                .persistenceDir(persistenceDir)
                .start();
        brooklynMgmt = brooklynLauncher.getServerDetails().getManagementContext();
        
        launcher = new BrooklynCampPlatformLauncherNoServer() {
            @Override
            protected LocalManagementContext newMgmtContext() {
                return (LocalManagementContext) brooklynMgmt;
            }
        };
        launcher.launch();
        
        platform = launcher.getCampPlatform();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() throws Exception {
        if (brooklynMgmt != null) Entities.destroyAll(brooklynMgmt);
        if (brooklynLauncher != null) brooklynLauncher.getServerDetails().getWebServer().stop();
        if (launcher != null) launcher.stopServers();
        if (persistenceDir != null) Os.deleteRecursively(persistenceDir);
    }

    protected Application rebind(Application origApp) throws Exception {
        RebindTestUtils.waitForPersisted(origApp);
        
        // Stop the old management context, so original entities won't interfere
        ((ManagementContextInternal)brooklynMgmt).terminate();
        if (brooklynLauncher != null) brooklynLauncher.getServerDetails().getWebServer().stop();
        if (launcher != null) launcher.stopServers();

        // Recreate the launcher etc
        brooklynLauncher = BrooklynLauncher.newInstance()
                .persistMode(PersistMode.REBIND)
                .persistenceDir(persistenceDir)
                .start();
        brooklynMgmt = brooklynLauncher.getServerDetails().getManagementContext();
        
        launcher = new BrooklynCampPlatformLauncherNoServer() {
            @Override
            protected LocalManagementContext newMgmtContext() {
                return (LocalManagementContext) brooklynMgmt;
            }
        };
        launcher.launch();
        platform = launcher.getCampPlatform();

        // Retrieve the newly reconstituted application
        String origAppId = origApp.getId();
        return Iterables.find(brooklynMgmt.getApplications(), EntityPredicates.idEqualTo(origAppId));
    }

    protected void waitForApplicationTasks(Entity app) {
        Set<Task<?>> tasks = BrooklynTaskTags.getTasksInEntityContext(brooklynMgmt.getExecutionManager(), app);
        LOG.info("Waiting on " + tasks.size() + " task(s)");
        for (Task<?> t : tasks) {
            t.blockUntilEnded();
        }
    }

    protected Entity createAndStartApplication(String yamlFileName) throws Exception {
        String yaml = loadYaml(yamlFileName, ImmutableMap.<String,String>of());
        return createAndStartApplication(Streams.newReaderWithContents(yaml));
    }

    protected Entity createAndStartApplication(Reader input) throws Exception {
        AssemblyTemplate at = platform.pdp().registerDeploymentPlan(input);
        Assembly assembly;
        try {
            assembly = at.getInstantiator().newInstance().instantiate(at, platform);
        } catch (Exception e) {
            LOG.warn("Unable to instantiate " + at + " (rethrowing): " + e);
            throw e;
        }
        LOG.info("Test - created " + assembly);
        final Entity app = brooklynMgmt.getEntityManager().getEntity(assembly.getId());
        LOG.info("App - " + app);
        return app;
    }

    protected Entity createStartWaitAndLogApplication(String resource) throws Exception {
        return createStartWaitAndLogApplication(resource, ImmutableMap.<String,String>of());
    }
    
    protected Entity createStartWaitAndLogApplication(String resource, Map<String, String> yamlRegexReplacements) throws Exception {
        String yaml = loadYaml(resource, yamlRegexReplacements);
        return createStartWaitAndLogApplication(Streams.newReaderWithContents(yaml));
    }

    protected Entity createStartWaitAndLogApplication(Reader input) throws Exception {
        String yaml = Streams.readFully(input);
        LOG.info("Creating app: "+yaml);
        
        Entity app = createAndStartApplication(Streams.newReaderWithContents(yaml));
        waitForApplicationTasks(app);

        LOG.info("App started:");
        Entities.dumpInfo(app);
        
        return app;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <T extends Entity> List<T> findEntities(Entity entity, Class<T> type) {
        Application app = entity.getApplication();
        Collection<Entity> result = app.getManagementContext().getEntityManager().findEntitiesInApplication(app, Predicates.instanceOf(type));
        return ImmutableList.<T>copyOf((Collection)result);
    }
    
    protected <T extends Entity> T findOnlyEntity(Entity entity, Class<T> type) {
        return (T) Iterables.getOnlyElement(findEntities(entity, type));
    }
    
    protected String loadYaml(String resource, Map<String, String> yamlRegexReplacements) {
        String yaml = new ResourceUtils(this).getResourceAsString(resource).trim();
        for (Map.Entry<String, String> entry : yamlRegexReplacements.entrySet()) {
            yaml = yaml.replaceAll(entry.getKey(), entry.getValue());
        }
        return yaml;
    }
}