package de.tlongo.serveranalytics.services.logfileservice;

import org.hibernate.annotations.Type;

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

    String requestMethod;

    @Column(length = 1024)
    String requestUri;

    String requestProtocol;

    public long getId() {
        return id;
    }

    String agent;

    @Type(type="de.tlongo.serveranalytics.services.logfileservice.LocalDateTimeUserType")
    LocalDateTime date;
    int status;

    @Override
    public String toString() {
        return "LogEntry{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", requestProtocol='" + requestProtocol + '\'' +
                ", agent='" + agent + '\'' +
                ", date=" + date +
                ", status=" + status +
                '}';
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }
}