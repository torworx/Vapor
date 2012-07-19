package evymind.vapor.app;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Pattern;

import evymind.vapor.core.utils.log.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.StringUtils;
import evyframework.common.io.Resource;
import evyframework.common.io.UrlResource;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evyframework.common.io.utils.FileUtils;
import evyframework.common.io.utils.JarUtils;
import evymind.vapor.core.utils.PatternMatcher;
import evymind.vapor.server.Connector;
import evymind.vapor.server.Server;

public class AppArchiveConfiguration extends AbstractConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AppArchiveConfiguration.class);

    private ResourcePatternResolver resolver = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();

    public static final String TEMPDIR_CONFIGURED = "evymind.vapor.tmpdirConfigured";
    public static final String CONTAINER_JAR_PATTERN = "evymind.vapor.server.app.ContainerIncludeJarPattern";
    public static final String APP_JAR_PATTERN = "evymind.vapor.server.app.AppIncludeJarPattern";

    /**
     * If set, to a list of URLs, these resources are added to the context resource base as a resource collection.
     */
    // public static final String RESOURCE_URLS = "evymind.vapor.resources";

    protected Resource preUnpackBaseResource;

    @Override
    public void preConfigure(final AppContext context) throws Exception {
        // Look for a work directory
        File work = findWorkDirectory(context);
        if (work != null)
            makeTempDirectory(work, context, false);

        // Make a temp directory for the app if one is not already set
        resolveTempDirectory(context);

        // Extract app if necessary
        unpack(context);

        // Apply an initial ordering to the jars which governs which will be scanned for META-INF
        // info and annotations. The ordering is based on inclusion patterns.
        String tmp = (String) context.getAttribute(APP_JAR_PATTERN);
        Pattern appPattern = (tmp == null ? null : Pattern.compile(tmp));
        tmp = (String) context.getAttribute(CONTAINER_JAR_PATTERN);
        Pattern containerPattern = (tmp == null ? null : Pattern.compile(tmp));

        // Apply ordering to container jars - if no pattern is specified, we won't
        // match any of the container jars
        PatternMatcher containerJarNameMatcher = new PatternMatcher() {
            @Override
            public void matched(URI uri) throws Exception {
                context.getMetadata().addContainerJar(new UrlResource(uri));
            }
        };
        ClassLoader loader = context.getClassLoader();
        while (loader != null && (loader instanceof URLClassLoader)) {
            URL[] urls = ((URLClassLoader) loader).getURLs();
            if (urls != null) {
                URI[] containerUris = new URI[urls.length];
                int i = 0;
                for (URL u : urls) {
                    try {
                        containerUris[i] = u.toURI();
                    } catch (URISyntaxException e) {
                        containerUris[i] = new URI(u.toString().replaceAll(" ", "%20"));
                    }
                    i++;
                }
                containerJarNameMatcher.match(containerPattern, containerUris, false);
            }
            loader = loader.getParent();
        }

        // Apply ordering to lib jars
        PatternMatcher appJarNameMatcher = new PatternMatcher() {
            @Override
            public void matched(URI uri) throws Exception {
                context.getMetadata().addAppJar(new UrlResource(uri));
            }
        };
        Resource[] jars = findJars(context);

        // Convert to uris for matching
        URI[] uris = null;
        if (jars != null) {
            uris = new URI[jars.length];
            int i = 0;
            for (Resource r : jars) {
                uris[i++] = r.getURI();
            }
        }
        appJarNameMatcher.match(appPattern, uris, true); // null is inclusive, no pattern == all jars match
    }

    @Override
    public void configure(AppContext context) throws Exception {
        // cannot configure if the context is already started
        if (context.isStarted()) {
            if (log.isDebugEnabled())
                log.debug("Cannot configure app " + context + " after it is started");
            return;
        }

        Resource appres = context.getBaseResource();

        // Add classes and lib classpaths
        if (appres != null && appres.getFile().isDirectory() && context.getClassLoader() instanceof AppClassLoader) {
            // Look for classes directory
            Resource classes = appres.createRelative("classes/");
            if (classes.exists())
                ((AppClassLoader) context.getClassLoader()).addClassPath(classes);

            // Look for jars
            Resource lib = appres.createRelative("lib/");
            if (lib.exists() || lib.getFile().isDirectory()) {
                ((AppClassLoader) context.getClassLoader()).addJars(lib);
            }
        }

        // Look for extra resource
        // Collection<Resource> resources = context.getAttribute(RESOURCE_URLS);
        // if (resources != null) {
        // Resource[] collection = new Resource[resources.size() + 1];
        // int i = 0;
        // collection[i++] = context.getBaseResource();
        // for (Resource resource : resources)
        // collection[i++] = resource;
        // context.setBaseResource(new ResourceCollection(collection));
        // }
    }

    @Override
    public void deconfigure(AppContext context) throws Exception {
        // delete temp directory if we had to create it or if it isn't called work
        Boolean tmpdirConfigured = (Boolean) context.getAttribute(TEMPDIR_CONFIGURED);

        if (context.getTempDirectory() != null && (tmpdirConfigured == null || !tmpdirConfigured.booleanValue())
                && !isTempWorkDirectory(context.getTempDirectory())) {
            FileUtils.delete(context.getTempDirectory());
            context.setTempDirectory(null);

            // clear out the context attributes for the tmp dir only if we had to
            // create the tmp dir
            context.setAttribute(TEMPDIR_CONFIGURED, null);
            context.setAttribute(AppContext.TEMPDIR, null);
        }

        // reset the base resource back to what it was before we did any unpacking of resources
        context.setBaseResource(preUnpackBaseResource);

        // TODO remove classpaths from classloader
    }

    /**
     * @see evymind.vapor.app.AbstractConfiguration#cloneConfigure(evymind.vapor.app.AppContext,
     *      evymind.vapor.app.AppContext)
     */
    @Override
    public void cloneConfigure(AppContext template, AppContext context) throws Exception {
        File tmpDir = File.createTempFile(AppArchiveConfiguration.getCanonicalNameForAppTmpDir(context), "", template
                .getTempDirectory().getParentFile());
        if (tmpDir.exists()) {
            FileUtils.delete(tmpDir);
        }
        tmpDir.mkdir();
        tmpDir.deleteOnExit();
        context.setTempDirectory(tmpDir);
    }

    /**
     * Get a temporary directory in which to unpack the sar etc etc. The algorithm for determining this is to check
     * these alternatives in the order shown:
     * <p/>
     * <p>
     * A. Try to use an explicit directory specifically for this app:
     * </p>
     * <ol>
     * <li>
     * Iff an explicit directory is set for this app, use it. Do NOT set delete on exit.</li>
     * <li>
     * Iff javax.service.context.tempdir context attribute is set for this app && exists && writeable, then use it.
     * Do NOT set delete on exit.</li>
     * </ol>
     * <p/>
     * <p/>
     * B. Create a directory based on global settings. The new directory will be called
     * "Vapor_"+host+"_"+port+"__"+context+"_"+virtualhost Work out where to create this directory:
     * <ol>
     * <li>
     * Iff $(vapor.home)/work exists create the directory there. Do NOT set delete on exit. Do NOT delete contents if
     * dir already exists.</li>
     * <li>
     * Iff WEB-INF/work exists create the directory there. Do NOT set delete on exit. Do NOT delete contents if dir
     * already exists.</li>
     * <li>
     * Else create dir in $(java.io.tmpdir). Set delete on exit. Delete contents if dir already exists.</li>
     * </ol>
     */
    public void resolveTempDirectory(AppContext context) {
        // If a tmp directory is already set, we're done
        File tmpDir = context.getTempDirectory();
        if (tmpDir != null && tmpDir.isDirectory() && tmpDir.canWrite()) {
            context.setAttribute(TEMPDIR_CONFIGURED, Boolean.TRUE);
            return; // Already have a suitable tmp dir configured
        }

        // No temp directory configured, try to establish one.
        // First we check the context specific, javax.service specified, temp directory attribute
        File serviceTmpDir = asFile(context.getAttribute(AppContext.TEMPDIR));
        if (serviceTmpDir != null && serviceTmpDir.isDirectory() && serviceTmpDir.canWrite()) {
            // Use as tmpDir
            tmpDir = serviceTmpDir;
            // Ensure Attribute has File object
            context.setAttribute(AppContext.TEMPDIR, tmpDir);
            // Set as TempDir in context.
            context.setTempDirectory(tmpDir);
            return;
        }

        try {
            // Put the tmp dir in the work directory if we had one
            File work = new File(System.getProperty("vapor.home"), "work");
            if (work.exists() && work.canWrite() && work.isDirectory()) {
                makeTempDirectory(work, context, false); // make a tmp dir inside work, don't delete if it exists
            } else {
                File baseTemp = asFile(context.getAttribute(AppContext.BASETEMPDIR));
                if (baseTemp != null && baseTemp.isDirectory() && baseTemp.canWrite()) {
                    // Use baseTemp directory (allow the funky Vapor_0_0_0_0.. subdirectory logic to kick in
                    makeTempDirectory(baseTemp, context, false);
                } else {
                    makeTempDirectory(new File(System.getProperty("java.io.tmpdir")), context, true); // make a tmpdir,
                    // delete if it
                    // already
                    // exists
                }
            }
        } catch (Exception e) {
            tmpDir = null;
            log.warn(Logs.IGNORED, e);
        }

        // Third ... Something went wrong trying to make the tmp directory, just make
        // a jvm managed tmp directory
        if (context.getTempDirectory() == null) {
            try {
                // Last resort
                tmpDir = File.createTempFile("VaporContext", "");
                if (tmpDir.exists())
                    FileUtils.delete(tmpDir);
                tmpDir.mkdir();
                tmpDir.deleteOnExit();
                context.setTempDirectory(tmpDir);
            } catch (IOException e) {
                tmpDir = null;
                throw new IllegalStateException("Cannot create tmp dir in " + System.getProperty("java.io.tmpdir")
                        + " for context " + context, e);
            }
        }
    }

    /**
     * Given an Object, return File reference for object. Typically used to convert anonymous Object from getAttribute()
     * calls to a File object.
     *
     * @param fileattr the file attribute to analyze and return from (supports type File and type String, all others return
     *                 null)
     * @return the File object, null if null, or null if not a File or String
     */
    private File asFile(Object fileattr) {
        if (fileattr == null) {
            return null;
        }
        if (fileattr instanceof File) {
            return (File) fileattr;
        }
        if (fileattr instanceof String) {
            return new File((String) fileattr);
        }
        return null;
    }

    public void makeTempDirectory(File parent, AppContext context, boolean deleteExisting) throws IOException {
        if (parent != null && parent.exists() && parent.canWrite() && parent.isDirectory()) {
            String temp = getCanonicalNameForAppTmpDir(context);
            File tmpDir = new File(parent, temp);

            if (deleteExisting && tmpDir.exists()) {
                if (!FileUtils.delete(tmpDir)) {
                    if (log.isDebugEnabled())
                        log.debug("Failed to delete temp dir " + tmpDir);
                }

                // If we can't delete the existing tmp dir, create a new one
                if (tmpDir.exists()) {
                    String old = tmpDir.toString();
                    tmpDir = File.createTempFile(temp + "_", "");
                    if (tmpDir.exists())
                        FileUtils.delete(tmpDir);
                    log.warn("Can't reuse " + old + ", using " + tmpDir);
                }
            }

            if (!tmpDir.exists())
                tmpDir.mkdir();

            // If the parent is not a work directory
            if (!isTempWorkDirectory(tmpDir)) {
                tmpDir.deleteOnExit();
            }

            if (log.isDebugEnabled())
                log.debug("Set temp dir " + tmpDir);
            context.setTempDirectory(tmpDir);
        }
    }

    public void unpack(AppContext context) throws IOException {
        Resource appres = context.getBaseResource();
        preUnpackBaseResource = context.getBaseResource();

        if (appres == null) {
            String sar = context.getSar();
            if (sar != null && sar.length() > 0)
                appres = context.newResource(sar);
            else
                appres = context.getBaseResource();

            // Accept aliases for SAR files
            // if (appres.getAlias() != null) {
            // log.debug(appres + " anti-aliased to " + appres.getAlias());
            // appres = context.newResource(appres.getAlias());
            // }
            File appfile = appres.getFile();
            if (log.isDebugEnabled())
                log.debug("Try app=" + appres + ", exists=" + appres.exists() + ", directory="
                        + appfile.isDirectory() + " file=" + (appfile));
            // Is the SAR usable directly?
            // if (app.exists() && !appAsFile.isDirectory() && !app.toString().startsWith("jar:")) {
            // // No - then lets see if it can be turned into a jar URL.
            // Resource jarApp = JarResource.newJarResource(app);
            // if (jarApp.exists() && jarApp.isDirectory())
            // app = jarApp;
            // }

            // If we should extract or the URL is still not usable
            if (appres.exists()
                    && ((context.isCopyDir() && appres.getFile() != null && appres.getFile().isDirectory()) ||
                        (context.isExtractSAR() && appres.getFile() != null && !appres.getFile().isDirectory()))) {
                // Look for sibling directory.
                File extractedAppDir = null;

                if (sar != null) {
                    // look for a sibling like "foo/" to a "foo.sar"
                    File sarFile = appfile;
                    if (sarFile != null && sarFile.getName().toLowerCase().endsWith(".sar")) {
                        File sibling = new File(sarFile.getParent(), sarFile.getName().substring(0,
                                sarFile.getName().length() - 4));
                        if (sibling.exists() && sibling.isDirectory() && sibling.canWrite())
                            extractedAppDir = sibling;
                    }
                }

                if (extractedAppDir == null) {
                    // Then extract it if necessary to the temporary location
                    extractedAppDir = new File(context.getTempDirectory(), "apps/");
                }

                if (appres.getFile() != null && appres.getFile().isDirectory()) {
                    // Copy directory
                    log.info("Copy " + appres + " to " + extractedAppDir);
                    FileUtils.copyDir(appres.getFile(), extractedAppDir);
                } else {
                    // Use a sentinel file that will exist only whilst the extraction is taking place.
                    // This will help us detect interrupted extractions.
                    File extractionLock = new File(context.getTempDirectory(), ".extract_lock");

                    if (!extractedAppDir.exists()) {
                        // it hasn't been extracted before so extract it
                        extractionLock.createNewFile();
                        extractedAppDir.mkdir();
                        log.info("Extract " + appres + " to " + extractedAppDir);
                        // Resource jar_web_app = JarResource.newJarResource(app);
                        // jar_web_app.copyTo(extractedAppDir);
                        JarUtils.unjar(appres.getFile(), extractedAppDir);
                        extractionLock.delete();
                    } else {
                        // only extract if the sar file is newer, or a .extract_lock file is left behind meaning a
                        // possible partial extraction
                        if (appres.lastModified() > extractedAppDir.lastModified() || extractionLock.exists()) {
                            extractionLock.createNewFile();
                            FileUtils.delete(extractedAppDir);
                            extractedAppDir.mkdir();
                            log.info("Extract " + appres + " to " + extractedAppDir);
                            // Resource jar_web_app = JarResource.newJarResource(app);
                            // jar_web_app.copyTo(extractedAppDir);
                            JarUtils.unjar(appres.getFile(), extractedAppDir);
                            extractionLock.delete();
                        }
                    }
                }
                appres = resolver.getResource(StringUtils.includeTrailingSlash(extractedAppDir.getCanonicalPath()));
            }

            // Now do we have something usable?
            if (!appres.exists()) {
                log.warn("Service application not found " + sar);
                throw new java.io.FileNotFoundException(sar);
            }

            context.setBaseResource(appres);

            if (log.isDebugEnabled())
                log.debug("app=" + appres);
        }

        // Do we need to extract WEB-INF/lib?
        // if (context.isCopyApp() && !context.isCopyWebDir()) {
        // Resource web_inf = app.addPath("WEB-INF/");
        //
        // File extractedAppDir = new File(context.getTempDirectory(), "webinf");
        // if (extractedAppDir.exists())
        // IO.delete(extractedAppDir);
        // extractedAppDir.mkdir();
        // Resource web_inf_lib = web_inf.addPath("lib/");
        // File appDir = new File(extractedAppDir, "WEB-INF");
        // appDir.mkdir();
        //
        // if (web_inf_lib.exists()) {
        // File appLibDir = new File(appDir, "lib");
        // if (appLibDir.exists())
        // IO.delete(appLibDir);
        // appLibDir.mkdir();
        //
        // log.info("Copying WEB-INF/lib " + web_inf_lib + " to " + appLibDir);
        // web_inf_lib.copyTo(appLibDir);
        // }
        //
        // Resource web_inf_classes = web_inf.addPath("classes/");
        // if (web_inf_classes.exists()) {
        // File appClassesDir = new File(appDir, "classes");
        // if (appClassesDir.exists())
        // IO.delete(appClassesDir);
        // appClassesDir.mkdir();
        // log.info("Copying WEB-INF/classes from " + web_inf_classes + " to "
        // + appClassesDir.getAbsolutePath());
        // web_inf_classes.copyTo(appClassesDir);
        // }
        //
        // web_inf = Resource.newResource(extractedAppDir.getCanonicalPath());
        //
        // ResourceCollection rc = new ResourceCollection(web_inf, app);
        //
        // if (log.isDebugEnabled())
        // log.debug("context.resourcebase = " + rc);
        //
        // context.setBaseResource(rc);
        // }
    }

    public File findWorkDirectory(AppContext context) throws IOException {
        // if (context.getBaseResource() != null) {
        // Resource web_inf = context.getApp();
        // if (web_inf != null && web_inf.exists()) {
        // return new File(web_inf.getFile(), "work");
        // }
        // }
        if (context.getBaseResource() != null) {
            return context.getBaseResource().createRelative("work").getFile();
        }
        return null;
    }

    /**
     * Check if the tmpDir itself is called "work", or if the tmpDir is in a directory called "work".
     *
     * @return true if File is a temporary or work directory
     */
    public boolean isTempWorkDirectory(File tmpDir) {
        if (tmpDir == null)
            return false;
        if (tmpDir.getName().equalsIgnoreCase("work"))
            return true;
        File t = tmpDir.getParentFile();
        if (t == null)
            return false;
        return (t.getName().equalsIgnoreCase("work"));
    }

    /**
     * Create a canonical name for a app temp directory. The form of the name is:
     * <code>"Vapor_"+host+"_"+port+"__"+resourceBase+"_"+context+"_"+virtualhost+base36_hashcode_of_whole_string</code>
     * <p/>
     * host and port uniquely identify the server context and virtual host uniquely identify the app
     *
     * @return the canonical name for the app temp directory
     */
    public static String getCanonicalNameForAppTmpDir(AppContext context) {
        StringBuffer canonicalName = new StringBuffer();
        canonicalName.append("vapor-");

        // get the host and the port from the first connector
        Server server = context.getServer();
        if (server != null) {
            Connector[] connectors = context.getServer().getConnectors();

            if (connectors.length > 0) {
                // Get the host
                String host = (connectors == null || connectors[0] == null ? "" : connectors[0].getHost());
                if (host == null)
                    host = "0.0.0.0";
                canonicalName.append(host);

                // Get the port
                canonicalName.append("-");
                // try getting the real port being listened on
                int port = (connectors == null || connectors[0] == null ? 0 : connectors[0].getPort());
                // if not available (eg no connectors or connector not started),
                // try getting one that was configured.
                if (port < 0)
                    port = connectors[0].getPort();
                canonicalName.append(port);
                canonicalName.append("-");
            }
        }

        // Resource base
        try {
            Resource resource = context.getBaseResource();
            if (resource == null) {
                // if (!StringUtils.hasLength(context.getSar())) { //(context.getSar() == null ||
                // context.getSar().length() == 0)
                // resource = context.newResource(context.getResourceBase());
                // }
                if (StringUtils.hasLength(context.getSar())) {
                    // Set dir or SAR
                    resource = context.newResource(context.getSar());
                } else {
                    log.warn("Can't generate baseResource dir as part of app tmp dir name");
                }
            }

            // TODO URI decode for format '%XX'
            String tmp = resource.getURL().getPath();
            if (tmp.endsWith("/"))
                tmp = tmp.substring(0, tmp.length() - 1);
            if (tmp.endsWith("!"))
                tmp = tmp.substring(0, tmp.length() - 1);
            // get just the last part which is the filename
            int i = tmp.lastIndexOf("/");
            canonicalName.append(tmp.substring(i + 1, tmp.length()));
            canonicalName.append("-");
        } catch (Exception e) {
            log.warn("Can't generate resourceBase as part of app tmp dir name", e);
        }

        // Context name
        String contextPath = context.getContextPath();
        contextPath = contextPath.replace('/', '_');
        contextPath = contextPath.replace('\\', '_');
        canonicalName.append(contextPath);

        // Virtual host (if there is one)
//		canonicalName.append("-");
//		String[] vhosts = context.getVirtualHosts();
//		if (vhosts == null || vhosts.length <= 0)
//			canonicalName.append("any");
//		else
//			canonicalName.append(vhosts[0]);

        // sanitize
        for (int i = 0; i < canonicalName.length(); i++) {
            char c = canonicalName.charAt(i);
            if (!Character.isJavaIdentifierPart(c) && "-.".indexOf(c) < 0)
                canonicalName.setCharAt(i, '.');
        }

        canonicalName.append("-");
        return canonicalName.toString();
    }

    /**
     * Look for jars in WEB-INF/lib
     *
     * @param context
     * @return the array of jar resources found within context
     * @throws Exception
     */
    protected Resource[] findJars(AppContext context) throws Exception {
        Resource appres = context.getBaseResource();
        if (appres == null || !appres.exists()) {
            return new Resource[]{};
        }

        return resolver.getResources(appres.getURI().getPath() + "/lib/*.jar");
    }
}
