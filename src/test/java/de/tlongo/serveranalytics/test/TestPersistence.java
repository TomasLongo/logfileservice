package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.services.logfileservice.LogEntry;
import de.tlongo.serveranalytics.services.logfileservice.LogEntryRepository;
import de.tlongo.serveranalytics.services.logfileservice.LogFileParser;
import org.hibernate.annotations.Type;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoPeriod;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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

    @Autowired
    FooRepo fooRepo;

    @Before
    public void clearDatabase() {
        repo.deleteAll();
    }

    @Test
    public void testDatePersistence() throws Exception {
        fooRepo.deleteAll();

        LocalDateTime current = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Foo foo = new Foo(current);
        foo = fooRepo.save(foo);

        Foo fetched = fooRepo.findOne(foo.id);
        assertThat(fetched, notNullValue());
        assertThat(fetched.date, equalTo(current));

        System.out.println(fetched.date.toString());
    }

    @Test
    public void testPersistLogEntry() throws Exception {
        LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LogEntry entry = new LogEntry();
        entry.setAgent("Mausi");
        entry.setDate(currentDateTime);
        repo.save(entry);

        LogEntry loaded = repo.findOne(entry.getId());
        assertThat(loaded, notNullValue());
        assertThat(loaded.getAgent(), equalTo("Mausi"));
        assertThat(loaded.getDate(), equalTo(currentDateTime));
    }

    @Test
    public void testPersistParsedFile() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogFile(new File("src/test/logdir/onefile/access.log.2"));
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long)logEntries.size()));


    }

    @Test
    public void testPersistParsedDirectory() throws Exception {
        List<LogEntry> logEntries = LogFileParser.parseLogDirectory("src/test/logdir/onefile");
        logEntries.forEach(entry -> {
            repo.save(entry);
        });

        long count = repo.count();
        assertThat(count, equalTo((long)logEntries.size()));
    }
}
