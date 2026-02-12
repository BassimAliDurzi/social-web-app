package com.socialwebapp.infra;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class PostgresAvailabilityTest {

    @Test
    void postgresMustBeReachableAtConfiguredHostPort() throws Exception {
        String jdbcUrl = resolveJdbcUrl();
        HostPort hp = parseHostPort(jdbcUrl);

        try (Socket socket = new Socket(hp.host, hp.port)) {
            // ok
        } catch (Exception ex) {
            fail("PostgreSQL is not reachable at " + hp.host + ":" + hp.port + ". Start Postgres before running tests.");
        }
    }

    private static String resolveJdbcUrl() throws Exception {
        String fromSysProp = System.getProperty("spring.datasource.url");
        if (fromSysProp != null && !fromSysProp.isBlank()) {
            return fromSysProp.trim();
        }

        Properties p = new Properties();
        try (InputStream in = PostgresAvailabilityTest.class.getClassLoader()
                .getResourceAsStream("application-test.properties")) {
            if (in == null) {
                throw new IllegalStateException("Missing application-test.properties on test classpath.");
            }
            p.load(in);
        }

        String fromProps = p.getProperty("spring.datasource.url");
        if (fromProps == null || fromProps.isBlank()) {
            throw new IllegalStateException("spring.datasource.url is missing in application-test.properties.");
        }
        return fromProps.trim();
    }

    private static HostPort parseHostPort(String jdbcUrl) {
        String s = jdbcUrl;
        if (s.startsWith("jdbc:")) {
            s = s.substring("jdbc:".length());
        }
        URI uri = URI.create(s);
        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int port = uri.getPort();
        if (port <= 0) {
            port = 5432;
        }
        return new HostPort(host, port);
    }

    private record HostPort(String host, int port) {}
}
