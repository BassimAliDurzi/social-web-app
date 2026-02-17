package com.socialwebapp.infra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class PostgresAvailabilityTest {

    @Test
    void postgresMustBeReachableAtConfiguredHostPort() {
        assumeTrue(
                env("CI_POSTGRES_CHECK").map("true"::equalsIgnoreCase).orElse(false),
                "Skipping Postgres availability check (CI_POSTGRES_CHECK != true)"
        );

        String jdbcUrl = resolveJdbcUrl();
        HostPort hp = parseHostPortFromJdbcUrl(jdbcUrl);

        boolean reachable = isTcpReachableWithRetries(hp.host(), hp.port(), 3000, 10, 300);

        assertTrue(
                reachable,
                "PostgreSQL is not reachable at " + hp.host() + ":" + hp.port()
                        + ". jdbcUrl=" + jdbcUrl
        );
    }


    private static String resolveJdbcUrl() {
        String direct = env("SPRING_DATASOURCE_URL").orElse(null);
        if (direct != null && !direct.isBlank()) {
            return direct;
        }

        String host = env("POSTGRES_HOST").orElse("localhost");
        String port = env("POSTGRES_PORT").orElse("5433");
        String db = env("POSTGRES_DB").orElse("social_web_test");
        return "jdbc:postgresql://" + host + ":" + port + "/" + db;
    }

    private static Optional<String> env(String name) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) return Optional.empty();
        return Optional.of(v);
    }

    private static boolean isTcpReachableWithRetries(
            String host,
            int port,
            int timeoutMillis,
            int attempts,
            int sleepMillis
    ) {
        for (int i = 1; i <= attempts; i++) {
            if (isTcpReachable(host, port, timeoutMillis)) {
                return true;
            }
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private static boolean isTcpReachable(String host, int port, int timeoutMillis) {
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), timeoutMillis);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static HostPort parseHostPortFromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return new HostPort("localhost", 5432);
        }

        String raw = jdbcUrl.trim();

        if (raw.startsWith("jdbc:postgresql:")) {
            String withoutPrefix = raw.substring("jdbc:postgresql:".length());
            if (withoutPrefix.startsWith("//")) {
                String uriPart = withoutPrefix.substring(2);
                int slash = uriPart.indexOf('/');
                String authority = (slash >= 0) ? uriPart.substring(0, slash) : uriPart;

                String host;
                int port;

                int colon = authority.lastIndexOf(':');
                if (colon >= 0) {
                    host = authority.substring(0, colon);
                    port = Integer.parseInt(authority.substring(colon + 1));
                } else {
                    host = authority;
                    port = 5432;
                }

                if (host.isBlank()) host = "localhost";
                return new HostPort(host, port);
            }
        }

        try {
            String asUri = raw.startsWith("jdbc:") ? raw.substring(5) : raw;
            URI uri = URI.create(asUri);
            String host = (uri.getHost() == null || uri.getHost().isBlank()) ? "localhost" : uri.getHost();
            int port = (uri.getPort() > 0) ? uri.getPort() : 5432;
            return new HostPort(host, port);
        } catch (Exception ignored) {
            return new HostPort("localhost", 5432);
        }
    }

    private record HostPort(String host, int port) {}
}
