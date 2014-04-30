package io.cloudsoft.usergrid;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.AbstractApplication;
import brooklyn.util.flags.SetFromFlag;

public abstract class AbstractUsergridApplication extends AbstractApplication {
    @SetFromFlag("usergridPropertiesTemplateUrl")
    ConfigKey<String> USERGRID_PROPERTIES_TEMPLATE_URL = UsergridTomcatServer.USERGRID_PROPERTIES_TEMPLATE_URL;
}
