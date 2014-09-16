package de.tlongo.serveranalytics;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;

import static spark.Spark.*;

/**
 * Created by tomas on 16.09.14.
 */
public class AnalyticsService {
    static GroovyShell groovyShell;
    public static void main(String[] args) throws IOException {
        Binding binding = new Binding();
        String[] a = {};
        binding.setVariable("args", a);
        groovyShell  = new GroovyShell(binding);


        get("/health", (request, response) -> {
            return "Im alive";
        });

        get("/articles", (request, response) -> {
            try {
                Script script = groovyShell.parse(new File("src/main/groovy/countaccess.groovy"));
                if (script == null) {
                    throw new RuntimeException("No script");
                }
                AnalyticsResult result = (AnalyticsResult)script.run();

                return "We found " + result.articeleCount.size() + " articles in the logs";
            } catch (IOException e) {
                return e.getStackTrace();

            }
        });
    }
}
