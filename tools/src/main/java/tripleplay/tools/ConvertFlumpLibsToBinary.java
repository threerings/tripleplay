//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.tools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import playn.core.Json;
import playn.core.json.JsonImpl;

import tripleplay.flump.LibraryData;

/**
 * A tool intended to convert from the standard Json flump library format to a faster-to-parse and
 * smaller binary format.
 */
public class ConvertFlumpLibsToBinary
{
    /**
     * Converts the Json flump libraries from the sourceDir into a binary version in the outputDir.
     */
    public static void convert (String sourceDir, String outputDir)
        throws IOException
    {
        List<File> jsonFiles = getJsonFiles(new File(sourceDir, "/assets/flump"));

        for (File jsonFile : jsonFiles) {
            String jsonTxt = new Scanner(jsonFile).useDelimiter("\\Z").next();
            Json.Object json = new JsonImpl().parse(jsonTxt);

            LibraryData lib = new LibraryData(json);

            File binFile = new File(jsonFile.getAbsolutePath().replace(sourceDir, outputDir).
                replace(".json", ".bin"));

            // Ensure all our parent directories are there.
            binFile.getParentFile().mkdirs();

            DataOutputStream ostream = new DataOutputStream(new FileOutputStream(binFile));
            try {
                lib.write(ostream);
            } finally {
                ostream.close();
            }
        }
    }

    public static void main (String[] args)
        throws IOException
    {
        if (args.length < 2) {
            throw new IllegalArgumentException(
                "Usage: java ConvertFlumpLibsToBinary <source dir> <output dir>");
        }

        convert(args[0], args[1]);
    }

    protected static List<File> getJsonFiles (File root)
    {
        if (root.isFile()) {
            List<File> result = new ArrayList<File>();
            result.add(root);
            return result;
        } else {
            List<File> result = new ArrayList<File>();
            for (final File file : root.listFiles()) {
                if (file.isDirectory()) {
                    // Descend...
                    result.addAll(getJsonFiles(file));
                } else if (file.getName().equals("library.json")) {
                    // This is one we're looking for...
                    result.add(file);
                }
            }
            return result;
        }
    }
}
