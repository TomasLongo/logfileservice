package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.services.logfileservice.LogEntry;
import de.tlongo.serveranalytics.services.logfileservice.LogFileParser;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

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
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(new ClassPathResource("/testconfig.properties").getFile()));
        properties.load(stream);
        stream.close();


        File logdir = new ClassPathResource("/logdir/fixed/oneentry.log.3").getFile();
        List<LogEntry> entries = LogFileParser.parseLogFile(logdir, "test");

        assertThat(entries, notNullValue());
        assertThat(entries.size(), is(1));

        // Check the first to see if the LogEntry object was built correctly
        // 202.46.57.65@22/Sep/2014:02:57:07 +0200@GET / HTTP/1.1@301@Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)
        LogEntry entry = entries.get(0);
        assertThat(entry, notNullValue());
        assertThat(entry.getStatus(), isOneOf(301, -9999));
        assertThat(entry.getAddress(), isOneOf("202.46.57.65", null));
        assertThat(entry.getRequestMethod(), isOneOf("GET", null));
        assertThat(entry.getRequestProtocol(), isOneOf("HTTP/1.1", null));
        assertThat(entry.getRequestUri(), isOneOf("/", null));
        assertThat(entry.getAgent(), isOneOf("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)", null));
        assertThat(entry.getDate(), isOneOf(Timestamp.valueOf(LocalDateTime.parse("22/Sep/2014:02:57:07 +0200", DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z"))), null));
    }

    @Test
    public void testParseInvalidEntry() throws Exception {
        List<LogEntry> list = LogFileParser.parseLogFile(new ClassPathResource("/logdir/fixed/invalidentry.log.1").getFile(), "Test");

        assertThat(list, notNullValue());
        assertThat(list, hasSize(1));
        assertThat(LogFileParser.isEntryValid(list.get(0)), is(false));

    }
}
