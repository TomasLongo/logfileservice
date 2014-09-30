package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.services.logfileservice.LogEntry;
import de.tlongo.serveranalytics.services.logfileservice.LogFileParser;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;


/**
 * Created by tomas on 18.09.14.
 */
public class TestLogFileParsing {
    @Test
    public void testLogFileParsing() throws Exception {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/test/resources/testconfig.properties"));
        properties.load(stream);
        stream.close();


        final List<LogEntry> entries = new ArrayList<>();
        Files.newDirectoryStream(new File((String)properties.get("logfileservice.logdir")).toPath()).forEach(path -> {
           entries.addAll(LogFileParser.parseLogFile(new File(path.toAbsolutePath().toString())));

        });

        assertThat(entries, notNullValue());
        assertThat(entries.size(), is(24));

        // Check the first to see if the LogEntry object was built correctly
        // 134.3.254.6@11/Sep/2014:22:00:58 +0200@GET / HTTP/1.1@301@Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36
        LogEntry entry = entries.get(0);
        assertThat(entry, notNullValue());
        assertThat(entry.getStatus(), is(301));
        assertThat(entry.getAddress(), equalTo("134.3.254.6"));
        assertThat(entry.getRequestMethod(), equalTo("GET"));
        assertThat(entry.getRequestProtocol(), equalTo("HTTP/1.1"));
        assertThat(entry.getRequestUri(), equalTo("/"));
        assertThat(entry.getAgent(), equalTo("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"));
        assertThat(entry.getDate(), equalTo(LocalDateTime.parse("11/Sep/2014:22:00:58 +0200", DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z"))));

    }
}
