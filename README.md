Brooklyn Usergrid
=================

This project contains Brooklyn entities and examples for [Apache Usergrid](https://usergrid.incubator.apache.org/).

### Contents
* [Configuring SSH](#configuringSsh)
* [Getting started](#gettingStarted)
* [Basic deployment](#basicDeployment)
* [Deploying with YAML](#yamlDeployment)
* [Deploying to the cloud](#cloudDeployment)

#### <a name="configuringSsh"></a>Configuring SSH
Brooklyn uses SSH to communicate with all nodes in a deployment, including localhost. If you have not already done so, you will
need to create an SSH key and configure access to your local machine. You can find instructions to do so
[here](http://brooklyncentral.github.io/use/guide/locations/) in the sections _Setting up an SSH key_ and _Localhost_

#### <a name="gettingStarted"></a>Getting started
The easiest way to get started is to start by downloading and extracting the binaries. To do so follow these simple instructions:

    % curl -o brooklyn-usergrid-0.1.0-SNAPSHOT-dist.tar.gz http://search.maven.org/remotecontent?filepath=io/cloudsoft/usergrid/brooklyn-usergrid/0.1.0-SNAPSHOT/brooklyn-usergrid-0.1.0-SNAPSHOT.tar.gz
    % tar -zxf brooklyn-usergrid-0.1.0-SNAPSHOT-dist.tar.gz
    % cd brooklyn-usergrid-0.1.0-SNAPSHOT-dist
    % ./start.sh launch

This will launch the Brooklyn console. Open the browser of your choice and navigate to [http://localhost:8081](http://localhost:8081).
You will then be presented with the option of starting a _Usergrid basic (2-node) app_ or a _Usergrid clustered (multi-node)
deployment_

#### <a name="basicDeployment"></a>Basic deployment
The simplest deployment scenario is to deploy a single Cassandra instance and a single Tomcat server to your local machine.
To do this, select _Usergrid basic (2-node) app_ and click _Next_

The next screen allows you to customize your deployment. The _usergrid-basic.properties_ file listed in the _Properties
template URL_ section can be found the in resources folder, which is automatically added to the classpath. For now, let's
stay with the default options and click _Finish_

Brooklyn will now install and launch Cassandra and Tomcat to your local machine and launch usergrid. Click on _Usergrid 
basic (2-node) deployment_ and expand the Usergrid application in the applications list to view the progress

Once the application is running, click on the UsergridTomcatServer and click on the link to
[http://localhost:8080/](http://localhost:8080) to verify the application

Congratulations! You have successfully launched Apache Usergrid

To stop the application, select the Usergrid application in the application list, click on the _Effectors_ tab, select _Stop_,
and click _Invoke_

You can also deploy Usergrid to a load-balanced Tomcat cluster and Cassandra cluster by choosing the _Usergrid clustered
(multi-node) deployment_

**N.B.**: When deploying to a single machine (e.g. localhost) it is not possible to deploy more
than one Cassandra node. If you attempt to deploy more than one Cassandra node, you will see an error informing you that the
JMX_SETUP_PREINSTALL step failed as the JMX registry port is already in use

#### <a name="yamlDeployment"></a>Deploying Usergrid using a YAML blueprint
In addition to the basic and clustered deployments included in the catalog, it is possible to define your own OASIS CAMP
compliant blueprints in YAML

Sample YAML blueprints for the basic and clustered deployments are provided in the yaml directory. To deploy usergrid simply
copy the YAML from one of the sample files, launch Brooklyn, select the _YAML_ tab in the _Create Application_ modal, paste
in the YAML, and click _Finish_. The YAML for the basic deployment is as follows:

````YAML
name: Usergrid basic (2-node) deployment
services:
- serviceType: io.cloudsoft.usergrid.UsergridTomcatServer
  brooklyn.config:
    version: 7.0.53
    wars.root: classpath://ROOT.war
    usergridPropertiesTemplateUrl: classpath://usergrid-basic.properties
    cassandraUrl: $brooklyn:component("theCassandraNode").attributeWhenReady("datastore.url") 
- serviceType: brooklyn.entity.nosql.cassandra.CassandraNode
  id: theCassandraNode
location: localhost
````

The `name` key is simply the display name for the application which will be shown in the Brooklyn web console. The `services`
key describes the services that will be deployed by Brooklyn and will be discussed further in this section. The `location`
key will be discussed further in the <a href="#cloudDeployment">Deploying to the cloud</a> section - when deploying to
localhost, the value of the location key should simply be `localhost`

In general, a Usergrid YAML deployment file will contain two services: a Usergrid Tomcat service and a Cassandra service.
The `serviceType` is the fully-qualified classname of a java class on the classpath; in this case we are using the basic
`UsergridTomcatServer` and `CassandraNode`

Additional configuration information is provided in the `brooklyn.config` key. The settings provided in the YAML above as as
follows:

* ````version````: This will determine the version of Tomcat that will be downloaded and installed. **N.B.**: This is the
Tomcat version, and not the Usergrid version
* ````wars.root````: Location of the .war file to be deployed. The latest version of the Usergrid war is provided in the 
brooklyn-usergrid jar file. If you wish to use an alternative Usergrid war file, simply place your preferred war file in the
resources directory and it will automatically be added to the classpath. You can then change this value to the war file you 
provided. **N.B.**: The jar file provided will take precedence on the classpath, so you should not name your war file ROOT.war.
Whatever you name it, it will be deployed as ROOT to the Tomcat server
* ````usergridPropertiesTemplateUrl````: This is the Usergrid properties file that will be deployed to the Tomcat server. Sample
files are provided in the resources directory. You can use your favourite text editor to edit or copy these files. For further
information about the properties files, please see the Usergrid documentation
* ````cassandraUrl````: In the sample basic properties file, the cassandra.url property is specified as ````cassandra.url=${entity.cassandraUrl}````.
In [cloud deployments](#cloudDeployment) the address of the Cassandra node may not be known in
advance, so a placeholder is used which is replaced at the point when the properties file is deployed. The 
````$brooklyn.component("theCassandraNode")```` section of the value tells Brooklyn that the value should be read from the 
component identified as _theCassandraNode_. This name should match the id given to the Cassandra node in the line ````id: 
theCassandraNode````. The ````attributeWhenReady("datastore.url")```` tells Brooklyn that it should read the value from the
_datastore.url_ attribute published by the node. If the attribute is not immediately available (for example if the machine is
still being provisioned or Cassandra has not yet started) then Brooklyn will automatically wait until the attribute is 
available

The usergrid-clustered YAML file introduces additional services and configuration:

````YAML
name: Usergrid clustered (multi-node) deployment
services:
- serviceType: brooklyn.entity.webapp.ControlledDynamicWebAppCluster
  brooklyn.config:
    initialSize: 3
    memberSpec:
      $brooklyn:entitySpec:
        type: io.cloudsoft.usergrid.UsergridTomcatServer
        brooklyn.config:
          version: 7.0.53
          wars.root: classpath://ROOT.war
          usergridPropertiesTemplateUrl: classpath://usergrid-clustered.properties
          cassandraDatacenter: $brooklyn:component("theCassandraNode") 
- serviceType: brooklyn.entity.nosql.cassandra.CassandraDatacenter
  id: theCassandraNode
  brooklyn.config:
    initialSize: 1
location: localhost
````

In this instance, rather than deploying a single `CassandraNode`, Brooklyn will deploy a cluster of replicated Cassanra nodes.
The ````initialSize```` config key tells Brooklyn the number of Cassandra nodes to create. In a production environment, this
should be three or more, however when deploying to localhost this is limited to one node

Additionally, instead of deploying a single `UsergridTomcatServer`, Brooklyn will deploy a `ControlledDynamicWebAppCluster`.
This consists of an nginx load balancer, and a cluster of one or more servers. The type of server deployed is determined
by the value of the ````memberSpec```` key. In this case we are providing an entitySpec for a `UsergridTomcatServer`. The
specification for the `UsergridTomcatServer` now includes the key ````cassandraDatacenter```` instead of 
````cassandraUrl````, allowing Brooklyn to examine the datacenter and build a URL comprising the host names and addresses
of all members of the cluster. Before compiling the URL, Brooklyn will wait for the datacenter to publish its `SERVICE_UP` sensor,
indicating that all members have been provisioned

#### <a name="cloudDeployment"></a>Deploying to the cloud
In a production environment, you will usually be deploying Usergrid to a cloud provider. In order to do this, you will need to
configure Brooklyn to use the identity and credential of your preferred cloud provider. This is usually done via the 
brooklyn.properties file. To deploy to Softlayer or AWS using YAML, simply create an empty brooklyn.properties file at 
`~/.brooklyn/brooklyn.properties` and add the following (you will need to replace the placeholders with your own credentials)

````bash
# Softlayer
brooklyn.location.jclouds.softlayer.identity=<your softlayer login name>
brooklyn.location.jclouds.softlayer.credential=<your softlayer authentication key>

# AWS
brooklyn.location.jclouds.aws-ec2.identity=<your aws access key id>
brooklyn.location.jclouds.aws-ec2.credential=<your aws secret access key>
````

You can then replace the _location_ section of your YAML as follows:

````bash
location:
  jclouds:softlayer:
    region: ams01 # Amsterdam
````

or for AWS:

````bash
location:
  jclouds:aws-ec2:
    region: us-west-1
````

To deploy to a cloud provider via the [Basic deployment](#basicDeployment) method, you will need to define _named locations_ in
your brooklyn.properties file as follows:

````bash
# Softlayer
brooklyn.location.jclouds.softlayer.identity=<your softlayer login name>
brooklyn.location.jclouds.softlayer.credential=<your softlayer authentication key>
brooklyn.location.named.Softlayer\ Amsterdam=jclouds:softlayer
brooklyn.location.named.Softlayer\ Amsterdam.region=ams01

# AWS
brooklyn.location.jclouds.aws-ec2.identity=<your aws access key id>
brooklyn.location.jclouds.aws-ec2.credential=<your aws secret access key>
brooklyn.location.named.AWS\ West\ 1=aws-ec2:eu-west-1
````

Once you have configured named locations, they will be available in the _Locations_ drop-down on the application customization
screen. If you have changed your brooklyn.properties file after launching Brooklyn, you will need to click the _Realod properties_
button on the home screen in order to make them available

For other cloud providers, a sample brooklyn.properties file is available [here](http://brooklyncentral.github.io/use/guide/quickstart/brooklyn.properties)

----
Copyright 2014 by Cloudsoft Corporation Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
