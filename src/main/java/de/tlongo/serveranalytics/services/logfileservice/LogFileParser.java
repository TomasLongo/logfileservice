package de.tlongo.serveranalytics.services.logfileservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

    /**
     * Parses a nginx log file line by line, extracts information and returns a list
     * of LogEntry objects
     *
     * @param logFile The log file to parse
     *
     * @return A list of LogEntry objects. An empty list if an error occurs.
     */
    public static List<LogEntry> parseLogFile(File logFile) {
        try {
            List<LogEntry> entryList = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(logFile));
            String line = null;
            while ((line = reader.readLine()) != null && !line.equals("")) {
                LogEntry entry = new LogEntry();
                String[] tokens = line.split("@");

                entry.setAddress(tokens[0]);
                entry.setDate(LocalDateTime.parse(tokens[1], formatter));
                entry.setRequestString(tokens[2]);
                entry.setStatus(Integer.parseInt(tokens[3]));
                entry.setAgent(tokens[4]);

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
}
