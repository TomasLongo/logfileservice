package de.tlongo.serveranalytics.services.logfileservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by tomas on 18.09.14.
 */
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    @Query(value = "SELECT entry FROM logentries entry WHERE entry.date >= :start AND entry.date < :end")
    List<LogEntry> findByDateRange(@Param("start") Timestamp start, @Param("end") Timestamp end);
}
