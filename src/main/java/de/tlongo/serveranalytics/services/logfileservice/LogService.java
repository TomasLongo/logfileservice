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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.tlongo.serveranalytics.services.logfileservice.JsonBuilder.jsonDocument;
import static spark.Spark.get;
import static spark.SparkBase.setIpAddress;

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

    List<LogEntry> latestProcessingCache = new ArrayList<>();

    Gson gson;

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
        setIpAddress("127.0.0.1");
        
        get("/health", (request, response) -> {
            response.status(200);
            return gson.toJson(jsonDocument().
                                    property("message", "LogService Alive!").
                                    property("code", 200).
                                    create());
        });

        /**
         * Returns the entries that were processed by the last regular persistence turn.
         *
         * The entries have been cached so that fetching doesnt require a roundtrip to
         * the db.
         */
        get("/entries/latest", (request, response) -> {
            final JsonObject json = new JsonObject();
            final JsonArray entryArray = new JsonArray();
            latestProcessingCache.forEach(entry -> {
                if (LogFileParser.isEntryValid(entry)) {
                    entryArray.add(entryToJson(entry));
                }
            });

            json.add("entries", entryArray);
            json.addProperty("count", latestProcessingCache.size());
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
        logger.info("Logfile directory is {}. Start parsing log files.", logdirPath);

        try {
            File logDir = new File(logdirPath);
            Files.newDirectoryStream(logDir.toPath()).forEach(path -> {
                String appName = path.getFileName().toString();
                logger.info("Parsing logs produced by '{}'", appName);
                List<LogEntry> logEntryList = LogFileParser.parseLogDirectory(path.toAbsolutePath().toString());
                if (logEntryList.size() > 0) {
                    dao.save(logEntryList);
                    clearLogDir(logdirPath);
                    latestProcessingCache = logEntryList;
                }


                logger.info("Done persisting {} entries", logEntryList.size());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

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
