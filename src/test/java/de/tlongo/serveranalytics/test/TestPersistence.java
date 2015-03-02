package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.services.logfileservice.LogEntry;
import de.tlongo.serveranalytics.services.logfileservice.LogEntryRepository;
import de.tlongo.serveranalytics.services.logfileservice.LogFileParser;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * Created by tomas on 19.09.14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/springdataconfig.xml")
public class TestPersistence {

    @Autowired
    LogEntryRepository repo;

    @Before
    public void clearDatabase() {
        repo.deleteAll();
    }

    @Test
    @Ignore
    public void testPersistLogEntry() throws Exception {
        LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LogEntry entry = new LogEntry();
        entry.setAgent("Mausi");
        entry.setDate(Timestamp.valueOf(currentDateTime));
        repo.save(entry);

        LogEntry loaded = repo.findOne(entry.getId());
        assertThat(loaded, notNullValue());
        assertThat(loaded.getAgent(), equalTo("Mausi"));
        assertThat(loaded.getDate(), equalTo(Timestamp.valueOf(currentDateTime)));

        System.out.println(loaded.getDate().toString());
    }

    @Test
    @Ignore
    public void testPersistParsedFile() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogFile(new File("src/test/logdir/fixed/access.log.2"), "Test");
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long)logEntries.size()));


    }

    @Test
    @Ignore
    public void testPersistParsedDirectory() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogDirectory("src/test/logdir/fixed");
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long) logEntries.size()));
    }

    @Test
    public void testFindByDateRange() throws Exception {
        String dir = new ClassPathResource("/logdir/fixed").getFile().toPath().normalize().toAbsolutePath().toString();
        List<LogEntry> logEntries = LogFileParser.parseLogDirectory(dir);
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long) logEntries.size()));

        Timestamp start = Timestamp.valueOf(LocalDateTime.of(2014, Month.OCTOBER, 18, 19, 57, 2).truncatedTo(ChronoUnit.DAYS));
        Timestamp end = Timestamp.valueOf(LocalDateTime.of(2014, Month.OCTOBER, 19, 19, 57, 2).truncatedTo(ChronoUnit.DAYS));
        List<LogEntry> entries = repo.findByDateRange(start, end);
        assertThat(entries, notNullValue());
        assertThat(entries, hasSize(6));


        start = Timestamp.valueOf(LocalDateTime.of(2014, Month.SEPTEMBER, 22, 19, 57, 2).truncatedTo(ChronoUnit.DAYS));
        end = Timestamp.valueOf(LocalDateTime.of(2014, Month.SEPTEMBER, 23, 19, 57, 2).truncatedTo(ChronoUnit.DAYS));
        entries = repo.findByDateRange(start, end);
        assertThat(entries, notNullValue());
        assertThat(entries, hasSize(73));
    }

    @Test
    public void testFindByIdList() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogDirectory("src/test/logdir/fixed");
        repo.save(logEntries);

        String idList = logEntries.stream().
                            map(entry -> String.valueOf(entry.getId())).
                            collect(Collectors.joining(","));

        System.out.println("<<<" + idList + ">>>");

        List<Long> longIds = Arrays.stream(idList.split(",")).map(stringid -> Long.parseLong(stringid)).collect(Collectors.toCollection(ArrayList::new));

        idList = longIds.stream().
                map(longId -> longId.toString()).
                collect(Collectors.joining(","));

        System.out.println("<<<" + idList + ">>>");

        long now = System.currentTimeMillis();
        List<LogEntry> finalFetch = repo.findAll(longIds);
        double time = (double)(System.currentTimeMillis() - now) / 1000.0;
        System.out.println("TIME: " + time);

        assertThat(finalFetch, hasSize(logEntries.size()));
        assertThat(finalFetch, equalTo(logEntries));
    }
}
