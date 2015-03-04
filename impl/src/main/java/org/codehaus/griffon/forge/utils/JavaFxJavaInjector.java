package org.codehaus.griffon.forge.utils;

import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.resource.URLResource;
import org.jboss.forge.addon.templates.Template;
import org.jboss.forge.addon.templates.TemplateFactory;
import org.jboss.forge.addon.templates.freemarker.FreemarkerTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jbuddha
 */
public class JavaFxJavaInjector extends LanguageFrameworkInjector {


    public JavaFxJavaInjector(Project project, ResourceFactory resourceFactory, TemplateFactory templateFactory) {
        super(project, resourceFactory, templateFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFolders() throws IOException {
        DirectoryResource directoryResource = (DirectoryResource) project.getRoot();
        createConfigFolder(directoryResource);
        createGriffonAppFolder(directoryResource);
        createMavenFolder(directoryResource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createConfigFolder(DirectoryResource rootDir) throws IOException {
        DirectoryResource configDirectory = rootDir.getOrCreateChildDirectory("config");
        DirectoryResource checkStyleDirectory = rootDir.getOrCreateChildDirectory("config/checkstyle");
        DirectoryResource condenarcDirectory = rootDir.getOrCreateChildDirectory("config/codenarc");

        FileResource headerFileTarget = (FileResource) configDirectory.getChild("HEADER");
        headerFileTarget.createNewFile();

        URL headerFileSourceUrl = getClass().getResource("/templates" + File.separator + "config" + File.separator + "HEADER.ftl");
        URLResource headerTemplateResource = resourceFactory.create(headerFileSourceUrl).reify(URLResource.class);
        Template template = templateFactory.create(headerTemplateResource, FreemarkerTemplate.class);

        Map<String, Object> templateContext = new HashMap<String, Object>();
        templateContext.put("yearvariable", "${year}");
        headerFileTarget.setContents(template.process(templateContext));

        // simply copying the files as there is no template processing required
        copyFileFromTemplates(checkStyleDirectory,
                "checkstyle.xml",
                "config" + File.separator + "checkstyle" + File.separator + "checkstyle.xml");

        copyFileFromTemplates(condenarcDirectory,
                "codenarc.groovy",
                "config" + File.separator + "codenarc" + File.separator + "codenarc.groovy");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMavenFolder(DirectoryResource rootDir) throws IOException {
        DirectoryResource mavenDir = rootDir.getOrCreateChildDirectory("maven");
        DirectoryResource distributionDir = rootDir.getOrCreateChildDirectory("maven/distribution");
        DirectoryResource binDir = rootDir.getOrCreateChildDirectory("maven/distribution/bin");

        copyFileFromTemplates(mavenDir,
                "ant-macros.xml",
                "maven" + File.separator + File.separator + "ant-macros.xml");

        copyFileFromTemplates(mavenDir,
                "assembly-descriptor.xml",
                "maven" + File.separator + File.separator + "assembly-descriptor.xml");

        copyFileFromTemplates(mavenDir,
                "post-site.xml",
                "maven" + File.separator + File.separator + "post-site.xml");

        copyFileFromTemplates(mavenDir,
                "prepare-izpack.xml",
                "maven" + File.separator + File.separator + "prepare-izpack.xml");

        copyFileFromTemplates(mavenDir,
                "process-resources.xml",
                "maven" + File.separator + File.separator + "process-resources.xml");

        String projectname = project.getRoot().getName();

        Map<String, Object> templateContext = new HashMap<String, Object>();
        templateContext.put("projectname", projectname);
        templateContext.put("JVM_OPTS", "${JVM_OPTS[@]}");

        String templatePath = "javafx-java" + File.separator + "maven" + File.separator + "distribution" + File.separator + "bin" + File.separator + "project.ftl";
        processTemplate(binDir, projectname, templatePath, templateContext);

        String batTemplatePath = "javafx-java" + File.separator + "maven" + File.separator + "distribution" + File.separator + "bin" + File.separator + "project.bat.ftl";
        processTemplate(binDir, projectname + ".bat", batTemplatePath, templateContext);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createGriffonAppFolder(DirectoryResource rootDir) throws IOException {
        DirectoryResource griffonAppDir = rootDir.getOrCreateChildDirectory("griffon-app");
        DirectoryResource confDir = griffonAppDir.getOrCreateChildDirectory("conf");
        DirectoryResource controllersDir = griffonAppDir.getOrCreateChildDirectory("controllers");
        DirectoryResource i18nDir = griffonAppDir.getOrCreateChildDirectory("i18n");
        DirectoryResource lifeStyleDir = griffonAppDir.getOrCreateChildDirectory("lifestyle");
        DirectoryResource modelsDir = griffonAppDir.getOrCreateChildDirectory("models");
        DirectoryResource resourcesDir = griffonAppDir.getOrCreateChildDirectory("resources");
        DirectoryResource servicesDir = griffonAppDir.getOrCreateChildDirectory("services");
        DirectoryResource viewsDir = griffonAppDir.getOrCreateChildDirectory("views");

        Map<String, String> variables = new HashMap<String, String>();
        variables.put("projectname",project.getRoot().getName());
        // TODO this can be even improved by changing the letter after - or _ to capital Case
        String simplename = project.getRoot().getName().replaceAll("[^A-Za-z0-9]","");
        char first = Character.toUpperCase(simplename.charAt(0));
        simplename = first + simplename.substring(1);
        variables.put("simplifiedprojectname",simplename);

        MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
        String topLevelPackage = metadataFacet.getProjectGroupName();
        if(topLevelPackage == null || topLevelPackage.length() == 0) {
            topLevelPackage = "org.example";
        }

        String modelClass = topLevelPackage + "." + simplename + "Model";
        String viewClass = topLevelPackage + "." + simplename + "View";
        String controllerClass = topLevelPackage + "." + simplename + "Controller";

        variables.put("modelclass",modelClass);
        variables.put("viewclass",viewClass);
        variables.put("controllerclass",controllerClass);

        processTemplate(confDir, "Config.java", "javafx-java" + File.separator + "griffon-app" + File.separator + "conf" + File.separator + "Config.java.ftl", variables);

    }


}
