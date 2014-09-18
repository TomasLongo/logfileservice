#!/home/tomas/groovy-2.3.6/bin/groovy
import de.tlongo.serveranalytics.AnalyticsResult

import java.time.format.DateTimeFormatter

/**
 * Created by Tomas Longo on 10.09.2014
 *
 * Counts the accesses to articles on the blog based on the entries of the access.log
 * of an nginx server.
 *
 * The script assumes following custom log format:
 *
 *      $remote_addr@$remote_user@$time_local@$request@$status@$http_user_agent
 *
 * This way, a log entry is easily parseable by splitting it on every '@'
 */

/**
 * Represents an entry in a log file and its tokens
 */
class LogEntry {
    def address
    def date
    String requestString
    def status
    def agent


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

    /**
     * Creates a request object out of the log entry
     */
    def createRequest() {
        Request request = new Request()

        def tokens = requestString.split(" ")
        request.setMethodFromString(tokens[0])
        request.uri = tokens[1]
        request.protocol = tokens[2]

        return request
    }
}

//TODO Make String out of the methods. We dont have to optimize things yet
class Request {
    static final int GET = 1
    static final int POST = 2
    static final int PUT = 3

    def setMethodFromString(method) {
        switch (method) {
            case "GET":
                this.method = GET
                break

            case "POST":
                this.method = POST
                break

            case "PUT":
                this.method = PUT
                break
        }
    }

    def setUri(String uri) {
        this.uri = uri
        if (uri.contains('.')) {
            isResource = true
        }
    }

    def method = 0
    def uri = ""

    def protocol = ""

    // Flag to mark if request fetched a resource or site
    def isResource = false


    @Override
    public String toString() {
        return "Request{" +
                "method=" + method +
                ", uri=" + uri +
                ", protocol=" + protocol +
                ", isResource=" + isResource +
                '}';
    }
}

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")

/**
 * Creates a LogEntry from a log line
 */
def parseLogLines = {String logLine ->
    LogEntry entry = new LogEntry()
    def tokens = logLine.split("@")

    entry.address = tokens[0]
    entry.date = formatter.parse(tokens[1])
    entry.requestString = tokens[2]
    entry.status = tokens[3]
    entry.agent = tokens[4]

    return entry
}


// Parsed log entries.
// Persist this to db to evaluate log Entries later on??
def logEntries = []

// Map holding the counter for every visited article
def articleCount = [:]

// Log lines, that could not be parsed out of the box
def errornousLogLines = []

def determineArticleVisited = { entryList ->
    entryList.each { LogEntry entry ->
        Request request = entry.createRequest()
        if (!request.isResource) {
            articleCount[request.uri] =  ++(articleCount.getOrDefault(request.uri, 0))
        }
    }
}

def validateLogEntry = { LogEntry logEntry ->
    // For now, we dont accept any reques containing escape sequences
    if (logEntry.requestString.contains("\\")) {
        return false
    }

    return true
}

// Parse cli args
def cli = new CliBuilder(usage: 'countaccess.groovy [-d dir]')
cli.d(longOpt: 'directory', 'directory containing log files', required: false, args: 1)
cli.h(longOpt: 'help', 'print help message', required: false)
cli.v(longOpt: 'verbose', 'show extended output', required: false)
cli.q(longOpt: 'quite', 'Dont show any messages', required: false)
cli.a(longOpt: 'analytics', 'Lets the script return the counted articles', required: false)
OptionAccessor options = cli.parse(args)

def dirToSearch = "."
if (options.h) {
    println cli.usage
    return
}
if (options.d) {
    dirToSearch = options.d
}

File dir = new File(dirToSearch)
def dirPath = dir.toPath().toAbsolutePath().normalize().toString()
if (!dir.isDirectory()) {
    println "$dirPath is not a directory."
    return
}


if (!options.q) {
    println "searching in directory $dirPath"
}

dir.eachFile { File file ->
    if (file.isFile() && file.name.endsWith(".log")) {
        file.eachLine { line ->
            def logEntry = parseLogLines(line)
            if (validateLogEntry(logEntry)) {
                logEntries << logEntry
            } else {
                errornousLogLines << logEntry
            }
        }
    }
}

if (!options.q) {
    println "Done parsing log files. Found ${logEntries.size()} valid entries and ${errornousLogLines.size()} invalid entries"
}

if (!options.q && options.v) {
    println "Extracted entries"
    logEntries.each { entry -> println entry}
}

determineArticleVisited(logEntries)


if (!options.q) {
    articleCount.each {article, count ->
        println "$article was visited $count times"
    }
}

if (options.a) {
    AnalyticsResult result = new AnalyticsResult()
    result.articeleCount.putAll(articleCount)

    return result
}