package io.vertx.example.util.parser;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.BasicSampleExtractor;
import io.vertx.example.util.kafka.InMemSamplePersister;
import io.vertx.example.util.kafka.SampleExtractor;
import io.vertx.example.util.kafka.SamplePersister;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class RegexpParserUrlTest {


    public static final String TEST_URL_1 = "http://test.pt.com/path?test=pt";
    public static final String TEST_URL_2 = "http://test.pt.com:8080/path?test=pt";

    @Test
    public void testParseUrl1() throws Exception {
        UrlParser parser = new RegExpUrlParser();
        UrlParseResult parse = parser.parse(TEST_URL_1);
        assertEquals(parse.getSchema(),"http");
        assertEquals(parse.getDomain(), "test.pt.com");
        assertEquals(parse.getPath(), "path");
        assertTrue(parse.getPort().get() == 8090);
        assertEquals(parse.getQuery().get(), "test=pt");
    }


}
