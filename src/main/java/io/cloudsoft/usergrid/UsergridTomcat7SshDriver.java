package io.cloudsoft.usergrid;

import java.util.Map;

import brooklyn.entity.basic.Attributes;
import brooklyn.entity.webapp.tomcat.Tomcat7SshDriver;
import brooklyn.entity.webapp.tomcat.TomcatServerImpl;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.os.Os;

import com.google.common.collect.ImmutableMap;

public class UsergridTomcat7SshDriver extends Tomcat7SshDriver implements UsergridTomcat7Driver {

    public UsergridTomcat7SshDriver(TomcatServerImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void customize() {
        super.customize();
        Map<String, String> replacements = ImmutableMap.of("hostAddress", entity.getAttribute(Attributes.ADDRESS));
        copyTemplate("classpath://usergrid-custom.properties", Os.mergePaths(getInstallDir(), "apache-tomcat-7.0.53", "lib", "usergrid-custom.properties"),
            replacements);
    }
}
