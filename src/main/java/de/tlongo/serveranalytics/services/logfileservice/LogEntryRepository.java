package de.tlongo.serveranalytics.services.logfileservice;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by tomas on 18.09.14.
 */
public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

}
