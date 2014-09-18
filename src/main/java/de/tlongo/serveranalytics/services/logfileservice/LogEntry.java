package de.tlongo.serveranalytics.services.logfileservice;

import java.time.LocalDateTime;

/**
 * Created by Tomas Longo on 18.09.14.
 */
public class LogEntry {
    String address;
    String requestString;
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