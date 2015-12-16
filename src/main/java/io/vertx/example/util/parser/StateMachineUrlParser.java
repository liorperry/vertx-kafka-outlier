package io.vertx.example.util.parser;

public class StateMachineUrlParser implements UrlParser {

    public static final String SCHEMA_DELIMITER = "://";

    @Override
    public UrlParseResult parse(String url) {
        int i = url.indexOf(SCHEMA_DELIMITER);

        return null;
    }
}
