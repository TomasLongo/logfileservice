package de.tlongo.serveranalytics.services.logfileservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.tlongo.serveranalytics.services.logfileservice.JsonBuilder.*;

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

    Gson gson;

    private long entryCount = 0;

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

        if (properties.getProperty("logfileservice.prettyjson").equals("true")) {
           gson = new GsonBuilder().setPrettyPrinting().create();
        } else {
            gson = new Gson();
        }

        initSpark();

        initScheduledPersistence();
    }

    private void initScheduledPersistence() {
        int scanIntervall = Integer.parseInt(properties.getProperty("logfileservice.scanintervall"));
        int startDelay = Integer.parseInt(properties.getProperty("logfileservice.startdelay"));
        logger.info("Service will scan directory every {} hours", scanIntervall);
        final ScheduledFuture<?> persistorHandle = scheduler.scheduleAtFixedRate(() -> persistLogEntries(), startDelay, scanIntervall, TimeUnit.HOURS);
    }

    private void initSpark() {
        get("/health", (request, response) -> {
            response.status(200);
            return gson.toJson(jsonDocument().
                                    property("message", "LogService Alive!").
                                    property("code", 200).
                                    create());
        });

        get("/entries/currentdate", (request, response) -> {
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            List<LogEntry> entries = dao.findByDateRange(Timestamp.valueOf(now), Timestamp.valueOf(now.plusDays(1)));

            final JsonObject json = new JsonObject();
            final JsonArray entryArray = new JsonArray();
            entries.forEach(entry -> {
                entryArray.add(entryToJson(entry));
            });

            json.add("entries", entryArray);
            json.addProperty("count", entries.size());
            json.addProperty("status", 200);

            return gson.toJson(json);
        });
    }

    private JsonObject entryToJson(LogEntry entry) {
        return jsonDocument().
                property("date", entry.getDate().toString()).
                property("agent", entry.getAgent()).
                property("address", entry.getAddress()).
                property("request-method", entry.getRequestMethod()).
                property("request-uri", entry.getRequestUri()).
                property("request-protocol", entry.getRequestProtocol()).
                property("request-status", entry.getStatus()).
                create();
    }

    /**
     * Starts the task of persisting log entries.
     *
     * Called periodically by the ScheduledExecutor
     */
    void persistLogEntries() {
        logger.info("Starting persisting log entries to db");

        String logdirPath = properties.getProperty("logfileservice.logdir");
        logger.info("Logfile directory is {}", logdirPath);

        logger.info("Parsing log files");

        entryCount = 0;

        if (properties.getProperty("logfileservice.persisting.perLogFile").equals("true")) {
            logger.info("Processing and persisting log files one by one");
            try {
                Files.newDirectoryStream(new File(logdirPath).toPath()).forEach( path -> {
                    File logFile = path.toFile();
                    if (logFile.isFile() && path.toAbsolutePath().toString().contains("log")) {
                        logger.info("Processing log file {}", path.toAbsolutePath());
                        try {
                            List<LogEntry> parsedEntries = LogFileParser.parseLogFile(path.toFile());
                            entryCount += parsedEntries.size();
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
            logEntryList.forEach(entry -> {
                dao.save(entry);
            });
            entryCount = logEntryList.size();
            clearLogDir(logdirPath);
        }


        logger.info("Done persisting {} entries", entryCount);
    }

    /**
     * Clears the log dir.
     *
     * Called after processing all log files in the directory.
     *
     * @param logdirPath The path of the directory to clear.
     */
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
