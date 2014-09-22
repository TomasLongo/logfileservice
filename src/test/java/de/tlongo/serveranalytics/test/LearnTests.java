package de.tlongo.serveranalytics.test;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Created by tomas on 18.09.14.
 */
public class LearnTests {

    @Test
    public void testDirectoryTraversal() throws Exception {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/test/resources/testconfig.properties"));
        properties.load(stream);
        stream.close();

        String foo = properties.getProperty("logfileservice.logdir");
        File logDir = new File(foo);

        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(logDir.toPath());

        directoryStream.forEach(path -> {
            System.out.println(path);
        });
    }
}
