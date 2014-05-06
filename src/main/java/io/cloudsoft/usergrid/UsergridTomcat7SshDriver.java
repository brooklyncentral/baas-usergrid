package io.cloudsoft.usergrid;

import brooklyn.entity.webapp.tomcat.Tomcat7SshDriver;
import brooklyn.entity.webapp.tomcat.TomcatServerImpl;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.os.Os;

public class UsergridTomcat7SshDriver extends Tomcat7SshDriver implements UsergridTomcat7Driver {

    public UsergridTomcat7SshDriver(TomcatServerImpl entity, SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void customize() {
        super.customize();
        String destFile = Os.mergePaths(getExpandedInstallDir(), "lib", "usergrid-custom.properties");
        String srcFile = entity.getConfig(UsergridTomcatServer.USERGRID_PROPERTIES_TEMPLATE_URL); 
        copyTemplate(srcFile, destFile);
    }
    
}
