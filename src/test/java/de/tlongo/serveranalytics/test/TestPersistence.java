package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.services.logfileservice.LogEntry;
import de.tlongo.serveranalytics.services.logfileservice.LogEntryRepository;
import de.tlongo.serveranalytics.services.logfileservice.LogFileParser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.List;

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
    public void foo() {
        repo.deleteAll();
    }

    @Test
    public void testPersistLogEntry() throws Exception {
        LogEntry entry = new LogEntry();
        entry.setAgent("Mausi");
        repo.save(entry);

        LogEntry loaded = repo.findOne(entry.getId());
        assertThat(loaded, notNullValue());
        assertThat(loaded.getAgent(), equalTo("Mausi"));
    }

    @Test
    public void testPersistParsedFile() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogFile(new File("src/test/logdir/access.2.log"));
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long)logEntries.size()));


    }

    @Test
    public void testPersistParsedDirectory() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogDirectory("src/test/logdir/");
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long)logEntries.size()));
    }
}
