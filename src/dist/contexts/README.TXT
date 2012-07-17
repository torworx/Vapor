This directory is scanned by the ContextDeployer instance
configured by the standard $VAPOR_HOME/etc/vapor.ecs configuration. 

It should contain XmlConfiguration files that describe individual
contexts to be deployed to the server.  This directory is scanned
for additions, removals and updates for hot deployment.

Frequenty the context configuration files here will reference
sar files or directories from $VAPOR_HOME/apps.  Care must be
taken to avoid a AppDeployer deploying duplicates of such
applications.

The directory ../contexts-available contains more example contexts
that may be deployed by being copied here.
