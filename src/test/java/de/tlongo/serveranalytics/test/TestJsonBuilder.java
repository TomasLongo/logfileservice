package de.tlongo.serveranalytics.test;

import com.google.gson.JsonObject;
import org.junit.Test;

import static de.tlongo.serveranalytics.services.logfileservice.JsonBuilder.*;

/**
 * Created by tomas on 10/13/14.
 */
public class TestJsonBuilder {
    @Test
    public void test() throws Exception {
        JsonObject json = jsonDocument().
                            property("name", "Tomas Longo").
                            property("age", 23).
                            property("male", 'y').
                            property("work", jsonDocument().
                                                property("name", "Company").
                                                property("business", "IT")).
                            property("boolean", true).
                            property("array", array("one", "two", true, 23, 23.33, 'c')).create();

        System.out.println(json.toString());
    }
}
