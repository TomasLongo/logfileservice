package de.tlongo.serveranalytics.services.logfileservice;

import javax.persistence.*;
import java.sql.Timestamp;

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

    Timestamp date;
    int status;

    // Name of the application that produced this entry
    String producedBy;

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

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
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

    public String getProducedBy() {
        return producedBy;
    }

    public void setProducedBy(String producedBy) {
        this.producedBy = producedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntry logEntry = (LogEntry) o;

        if (id != logEntry.id) return false;
        if (status != logEntry.status) return false;
        if (address != null ? !address.equals(logEntry.address) : logEntry.address != null) return false;
        if (agent != null ? !agent.equals(logEntry.agent) : logEntry.agent != null) return false;
        if (date != null ? !date.equals(logEntry.date) : logEntry.date != null) return false;
        if (producedBy != null ? !producedBy.equals(logEntry.producedBy) : logEntry.producedBy != null) return false;
        if (requestMethod != null ? !requestMethod.equals(logEntry.requestMethod) : logEntry.requestMethod != null)
            return false;
        if (requestProtocol != null ? !requestProtocol.equals(logEntry.requestProtocol) : logEntry.requestProtocol != null)
            return false;
        if (requestUri != null ? !requestUri.equals(logEntry.requestUri) : logEntry.requestUri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (requestMethod != null ? requestMethod.hashCode() : 0);
        result = 31 * result + (requestUri != null ? requestUri.hashCode() : 0);
        result = 31 * result + (requestProtocol != null ? requestProtocol.hashCode() : 0);
        result = 31 * result + (agent != null ? agent.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + (producedBy != null ? producedBy.hashCode() : 0);
        return result;
    }
}