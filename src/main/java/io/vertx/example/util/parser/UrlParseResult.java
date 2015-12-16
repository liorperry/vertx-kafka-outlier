package io.vertx.example.util.parser;

import java.util.Optional;

/**
 * Scheme: http
 * Domain: test.pt.com
 * Port: 8090 (optional)
 * Path: path
 * Query String: test=pt (optional)
 */
public class UrlParseResult {
    public String schema;
    public String domain;
    public Optional<Integer> port = Optional.empty();
    public String path;
    public Optional<String> query = Optional.empty();

    private UrlParseResult() {}

    public UrlParseResult(String schema, String domain, Optional<Integer> port, String path, Optional<String> query) {
        this.schema = schema;
        this.domain = domain;
        this.port = port;
        this.path = path;
        this.query = query;
    }

    public String getSchema() {
        return schema;
    }

    public String getDomain() {
        return domain;
    }

    public Optional<Integer> getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getQuery() {
        return query;
    }

    /**
     * fluent UrlParseResult builder
     */
    public static class Builder {
        private UrlParseResult instance;

        private Builder() {
            instance = new UrlParseResult();
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder schema(String schema) {
            instance.schema = schema;
            return this;
        }

        public Builder domain(String domain) {
            instance.domain = domain;
            return this;
        }

        public Builder path(String path) {
            instance.path = path;
            return this;
        }

        public Builder query(Optional<String> query) {
            instance.query = query;
            return this;
        }

        public Builder port(Optional<Integer> port) {
            instance.port = port;
            return this;
        }

        public UrlParseResult build() {
            return instance;
        }
    }
}
