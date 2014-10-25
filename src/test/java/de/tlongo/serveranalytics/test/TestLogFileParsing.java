package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.services.logfileservice.LogEntry;
import de.tlongo.serveranalytics.services.logfileservice.LogFileParser;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.sql.Timestamp;
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
           entries.addAll(LogFileParser.parseLogFile(new File(path.toAbsolutePath().toString()), "Test"));

        });

        assertThat(entries, notNullValue());
        assertThat(entries.size(), is(79));

        // Check the first to see if the LogEntry object was built correctly
        // 202.46.57.65@22/Sep/2014:02:57:07 +0200@GET / HTTP/1.1@301@Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)
        LogEntry entry = entries.get(0);
        assertThat(entry, notNullValue());
        assertThat(entry.getStatus(), is(301));
        assertThat(entry.getAddress(), equalTo("202.46.57.65"));
        assertThat(entry.getRequestMethod(), equalTo("GET"));
        assertThat(entry.getRequestProtocol(), equalTo("HTTP/1.1"));
        assertThat(entry.getRequestUri(), equalTo("/"));
        assertThat(entry.getAgent(), equalTo("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)"));
        assertThat(entry.getDate(), equalTo(Timestamp.valueOf(LocalDateTime.parse("22/Sep/2014:02:57:07 +0200", DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")))));
    }

    @Test
    public void testParseInvalidEntry() throws Exception {
        List<LogEntry> list = LogFileParser.parseLogFile(new File("src/test/logdir/invalidentry/invalidentry.log"), "Test");

        assertThat(list, notNullValue());
        assertThat(list, hasSize(1));
        assertThat(LogFileParser.isEntryValid(list.get(0)), is(false));

    }
}
