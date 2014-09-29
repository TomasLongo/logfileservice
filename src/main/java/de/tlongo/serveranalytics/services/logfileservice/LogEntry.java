package de.tlongo.serveranalytics.services.logfileservice;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by Tomas Longo on 18.09.14.
 */

@Entity(name="logentries")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String address;

    @Column(length = 2048)
    String requestString;

    public long getId() {
        return id;
    }

    String agent;
    LocalDateTime date;
    int status;

    @Override
    public String toString() {
        return "LogEntry{" +
                "address=" + address +
                ", date=" + date +
                ", request=" + requestString +
                ", status=" + status +
                ", agent=" + agent +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRequestString() {
        return requestString;
    }

    public void setRequestString(String requestString) {
        this.requestString = requestString;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}