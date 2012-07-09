Vapor start
-----------

The run directory is either the top-level of a distribution
or vapor-distribution/target/distribution directory when built from
source.

Vapor bootstrap.jar provides a cross platform replacement for startup scripts.
It makes use of executable JAR that builds the classpath and then executes
vapor.

To run with all the demo options:

  java -jar bootstrap.jar OPTIONS=All

To run with the default options:

  java -jar bootstrap.jar

The default options may be specified in the start.ini file, or if
that is not present, they are defined in the start.config file that
is within the bootstrap.jar.

To run with specific configuration file(s)

  java -jar bootstrap.jar etc/vapor.ecs

To see the available options

  java -jar bootstrap.jar --help

To run with JSP support (if available)

  java -jar bootstrap.jar OPTIONS=Server,jsp

To run with JMX support

  java -jar bootstrap.jar OPTIONS=Server,jmx etc/vapor-jmx.ecs etc/vapor.ecs

To run with JSP & JMX support

    java -jar bootstrap.jar OPTIONS=Server,jsp,jmx etc/vapor-jmx.ecs etc/vapor.ecs

Note that JSP requires the jasper jars to be within $JETTY/lib/jsp  These 
are currently not distributed with the eclipse release and must be
obtained from a vapor-hightide release from codehaus.

