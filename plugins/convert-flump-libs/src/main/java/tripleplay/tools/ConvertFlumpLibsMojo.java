//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.tools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import playn.core.json.JsonImpl;

import tripleplay.flump.LibraryData;

@Mojo(name="flump", defaultPhase=LifecyclePhase.PROCESS_RESOURCES)
public class ConvertFlumpLibsMojo extends AbstractMojo
{
    @Parameter(required=true, defaultValue="${project}")
    public MavenProject project;

    /** The root location for resources. */
    @Parameter(required=true, defaultValue="src/main/resources")
    public File resourceRoot;

    /** The root location to save the converted binary files. By default, uses the build output. */
    @Parameter
    public File outputRoot;

    /** The path within {@link #resourceRoot} to look for json files. */
    @Parameter(defaultValue="assets/flump", property="flump.path")
    public String path;

    /** Specifies whether the conversion should skip source files with older modification times
     * than their binary counterparts. */
    @Parameter(defaultValue="true", property="flump.useModificationTimes")
    public boolean useModificationTimes;

    @Override public void execute ()
            throws MojoExecutionException, MojoFailureException {
        if (outputRoot == null) outputRoot = new File(project.getBuild().getOutputDirectory());
        try {
            int count = convert(path);
            getLog().info("Converted " + count + " out of date libraries");
        } catch (IOException ex) {
            throw new MojoExecutionException("", ex);
        }
    }

    protected int convert (String root)
            throws IOException {
        int count = 0;
        File rootDir = new File(resourceRoot, root);
        for (String item : rootDir.list()) {
            String itemPath = root + File.separatorChar + item;
            File child = new File(rootDir, item);
            if (child.isDirectory()) {
                count += convert(itemPath);
                continue;
            }
            if (item.equals("library.json")) {
                count += convert(child, itemPath);
            }
        }
        return count;
    }

    protected LibraryData readLib (File jsonFile)
            throws IOException {
        Scanner scanner = new Scanner(jsonFile);
        try {
            return new LibraryData(new JsonImpl().parse(scanner.useDelimiter("\\Z").next()));
        } finally {
            scanner.close();
        }
    }

    protected int convert (File jsonFile, String jsonPath)
            throws IOException {
        File bin = new File(outputRoot, jsonPath.replace(".json", ".bin"));
        if (useModificationTimes && bin.exists() && bin.lastModified() > jsonFile.lastModified()) {
            getLog().debug("Skipping up to date file " + bin);
            return 0;
        }

        getLog().debug("Converting " + bin);
        bin.getParentFile().mkdirs();
        DataOutputStream ostream = new DataOutputStream(new FileOutputStream(bin));
        try {
            readLib(jsonFile).write(ostream);
        } finally {
            ostream.close();
        }
        return 1;
    }
}
