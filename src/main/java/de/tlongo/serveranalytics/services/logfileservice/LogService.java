package de.tlongo.serveranalytics.services.logfileservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManagerFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;

/**
 * Created by tomas on 16.09.14.
 */

/**
 * Extracts relevant information from nginx log files and stores it in a database.
 */

/*
    Paths:
        - /entries/currentday --> Gets all log entries from today
        - /entries/{date}     --> Gets log entries of a specific date
 */
@Service
public class LogService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Logger logger = LoggerFactory.getLogger(LogService.class);
    Properties properties;

    @Autowired
    LogEntryRepository dao;

    public static void main(String[] args) throws IOException {
        ApplicationContext springContext = new ClassPathXmlApplicationContext("/springdataconfig.xml");

        LogService service = springContext.getBean(LogService.class);
        service.startService();
    }

    private void startService() throws IOException {
        logger.info("Starting LogService");
        logger.info("Init Spring Data");

        if (dao == null) {
            logger.error("Could not create repository for db access");
            throw new RuntimeException("Could not create repository for db access");
        }
        logger.info("Spring is up and running");

        initProperties();

        get("/health", (request, response) -> {
            return "LogFileService alive";
        });

        get("/entries/currentdate", (request, response) -> {
            return "Log Entries from today";
        });

        final ScheduledFuture<?> persistorHandle = scheduler.scheduleAtFixedRate(() -> persistLogEntries(), 10, 10, TimeUnit.SECONDS);
    }


    void persistLogEntries() {
        logger.info("Starting persisting log entries to db");

        String logdirPath = properties.getProperty("logfileservice.logdir");
        logger.info("Logfile directory is {}", logdirPath);

        logger.info("Parsing log files");

        if (properties.getProperty("logfileservice.persisting.perLogFile").equals("true")) {
            logger.info("Processing and persisting log files one by one");
            try {
                Files.newDirectoryStream(new File(logdirPath).toPath()).forEach( path -> {
                    File logFile = path.toFile();
                    if (logFile.isFile() && path.toAbsolutePath().toString().contains("log")) {
                        try {
                            List<LogEntry> parsedEntries = LogFileParser.parseLogFile(path.toFile());
                            dao.save(parsedEntries);
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.error("Error parsing logfile", e);
                        }
                    }
                });
            } catch (IOException e) {
                logger.error("Error parsing logdir", e);
            }
        } else {
            logger.info("Processing and persisting log files all at once");
            List<LogEntry> logEntryList = LogFileParser.parseLogDirectory(logdirPath);
            logger.info("Persisting {} entries to db.", logEntryList.size());
            logEntryList.forEach(entry -> {
                dao.save(entry);
            });
            clearLogDir(logdirPath);
        }


        logger.info("Done persisting log entries");

    }

    private void clearLogDir(String logdirPath) {
        logger.info("Clearing log files from directory.");

        File dir = new File(logdirPath);
        DirectoryStream<Path> directoryStream = null;
        try {
            directoryStream = Files.newDirectoryStream(dir.toPath());
            directoryStream.forEach(path -> {
                File logFile = new File(path.toAbsolutePath().toString());
                if (!logFile.isDirectory() && logFile.getName().contains("log")) {
                    try {
                        logger.info("deleting log file {}", logFile.getAbsolutePath());
                        Files.delete(logFile.toPath());
                    } catch (IOException e) {
                        logger.error("Error deleting log file {}\n{}", logFile.getName(), e);
                        throw new RuntimeException("Error deleting log file");
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Error deleting dir {}\n{}", logdirPath, e);
        }

        logger.info("Done clearing log directory");
    }

    private void initProperties() throws IOException {
        URL url = LogService.class.getClassLoader().getResource("config.properties");
        properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(url.getFile()));
        properties.load(stream);
        stream.close();
    }
}
