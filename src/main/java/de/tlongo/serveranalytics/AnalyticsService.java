package de.tlongo.serveranalytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static spark.Spark.*;

/**
 * Created by tomas on 16.09.14.
 */
public class AnalyticsService {
    static GroovyShell groovyShell;
    static Gson gson;
    public static void main(String[] args) throws IOException {
        Binding binding = new Binding();
        String[] a = {"-qa"};
        binding.setVariable("args", a);
        groovyShell  = new GroovyShell(binding);

        GsonBuilder builder = new GsonBuilder();
        gson = builder.setPrettyPrinting().create();


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

                JsonObject jsonReturn = new JsonObject();
                jsonReturn.addProperty("code", 200);
                jsonReturn.add("analytics", createArticleCountObject(result.articeleCount));


                return gson.toJson(jsonReturn);
            } catch (IOException e) {
                return e.getStackTrace();
            }
        });
    }

    static JsonObject createArticleCountObject(Map<String, Integer> articles) {
        JsonObject articleCount = new JsonObject();
        JsonObject json = new JsonObject();
        articles.forEach((article, count) -> {
            json.addProperty(article, count);
        });

        articleCount.add("articleCount", json);

        return articleCount;
    }
}
