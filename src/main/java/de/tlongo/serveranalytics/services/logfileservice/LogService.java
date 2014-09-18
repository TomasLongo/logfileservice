package de.tlongo.serveranalytics.services.logfileservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class LogService {
    static Logger logger = LoggerFactory.getLogger(LogService.class);
    static Properties properties;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) throws IOException {
        final ScheduledFuture<?> persistorHandle = scheduler.scheduleAtFixedRate(() -> System.out.println("Persisting..."), 0, 10, TimeUnit.SECONDS);

        get("/health", (request, response) -> {
            return "LogFileService alive";
        });

        get("/entries/currentdate", (request, response) -> {
            return "Log Entries from today";
        });
    }


    static void persistLog() {
        String logdirpath = System.getProperty("logdir");

        File logdir = new File(logdirpath);
        if (!logdir.isDirectory()) {
            logger.error("{} is not a directory.", logdirpath);
            return;
        }
    }

    private static void initProperties() throws IOException {
        properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream("src/main/resources/testconfig.properties"));
        properties.load(stream);
        stream.close();
    }
}
