package de.tlongo.serveranalytics.services.logfileservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomas on 18.09.14.
 */
public class LogFileParser {
    static Logger logger = LoggerFactory.getLogger(LogFileParser.class);
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
    private static final List<LogEntry> EMPTY_LIST = new ArrayList<>();

    public static boolean isEntryValid(LogEntry entry) {
        return entry.getStatus() != -9999;
    }

    /**
     * Parses a nginx log file line by line, extracts information and returns a list
     * of LogEntry objects
     *
     * @param logFile The log file to parse
     *
     * @return A list of LogEntry objects. An empty list if an error occurs.
     */
    public static List<LogEntry> parseLogFile(File logFile, String producingApp) {
        try {
            List<LogEntry> entryList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;

                LogEntry entry = new LogEntry();
                String[] tokens = line.split("@");

                if (tokens.length != 5) {
                    // Trying to split this log line into its components by the delimeter '@'
                    // caused an error because '@' was part of the line itself.
                    // Mark entry as invalid in order to get some metrics on how often this
                    // occurs. We might have to change the separator.
                    entry.setStatus(-9999);
                } else {
                    entry.setAddress(tokens[0]);
                    entry.setDate(Timestamp.valueOf(LocalDateTime.parse(tokens[1], formatter)));
                    entry.setStatus(Integer.parseInt(tokens[3]));
                    entry.setAgent(tokens[4]);
                    entry.setProducedBy(producingApp);

                    String[] requestTokens = splitRequestString(tokens[2]);
                    if (requestTokens.length != 3) {
                        entry.setStatus(-9999);
                    } else {
                        entry.setRequestMethod(requestTokens[0]);
                        entry.setRequestUri(requestTokens[1]);
                        entry.setRequestProtocol(requestTokens[2]);
                    }
                }

                entryList.add(entry);
            }
            return entryList;
        } catch (FileNotFoundException e) {
            logger.error("Could not find file under path {}", logFile.getAbsolutePath());
            return EMPTY_LIST;
        } catch (IOException e) {
            logger.error("Error parsing logfile {}\n{}", logFile.getAbsolutePath(), e);
            return EMPTY_LIST;
        }
    }

    private static String[] splitRequestString(String requestString) {
        // tokens = [method, uri, protocol]
        String[] tokens = requestString.split(" ");



        return tokens;
    }

    /**
     * Parses log files located in the specified directory.
     *
     * @return A List of log entries extracted from the files in the directory.
     */
    public static List<LogEntry> parseLogDirectory(String logDir) {
        File dir = new File(logDir);
        if (!dir.isDirectory()) {
            logger.error("{} is not a directory", logDir);
            return EMPTY_LIST;
        }

        logger.info("Parsing log files in directory {}", dir.getAbsolutePath());
        try {
            List<LogEntry> logEntryList = new ArrayList<>();
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir.toPath());
            directoryStream.forEach(path -> {
                File logFile = new File(path.toAbsolutePath().toString());
                if (!logFile.isDirectory() && logFile.getName().contains("log")) {
                    logger.info("Parsing logfile {}", logFile.getAbsolutePath());
                    List<LogEntry> list = parseLogFile(logFile, dir.toPath().getFileName().toString());

                    logEntryList.addAll(list);
                }
            });

            return logEntryList;
        } catch (IOException e) {
            logger.error("Could not find directory under {}", logDir);
            return EMPTY_LIST;
        }
    }
}
