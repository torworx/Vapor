
VAPOR
=====

BUILDING VAPOR
==============

Vapor uses gradle as its build system.  Gradle will fetch
the dependancies, build the server and assemble a runnable
version:

  gradle install



RUNNING VAPOR
=============

The run directory is either the top-level of a binary release
or build/XXXX directory when built from
source.

To run with the default options:

  java -jar bootstrap.jar

To see the available options and the default arguments
provided by the bootstrap.ini file:

  java -jar bootstrap.jar --help

To run with extra configuration file(s) appended, eg SSL

  java -jar bootstrap.jar etc/vapor-ssl.xml

To run with properties 

  java -jar bootstrap.jar vapor.port=8081

To run with extra configuration file(s) prepended, eg logging & jmx

  java -jar bootstrap.jar --pre=etc/vapor-logging.xml --pre=etc/vapor-jmx.xml 

To run without the args from bootstrap.ini 

  java -jar bootstrap.jar --ini OPTIONS=Server etc/vapor.xml etc/vapor-deploy.xml

to list the know OPTIONS:

  java -jar bootstrap.jar --list-options

