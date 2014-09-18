package de.tlongo.serveranalytics.test;

import de.tlongo.serveranalytics.AnalyticsResult;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;


import java.io.File;

/**
 * Created by tomas on 17.09.14.
 */
public class TestBlogAnalytics {
    static GroovyShell shell;

    @BeforeClass
    public static void setupShell() {
        Binding shellBinding = new Binding();
        String[] args = {"-a"};
        shellBinding.setVariable("args", args);

        shell = new GroovyShell(shellBinding);
    }

    @Test
    public void testCountAccess() throws Exception {
        Script script = shell.parse(new File("src/main/groovy/countaccess.groovy"));

        AnalyticsResult result = (AnalyticsResult)script.run();

        assertThat(result, notNullValue());
        assertThat(result.articeleCount.size(), is(3));
    }
}
