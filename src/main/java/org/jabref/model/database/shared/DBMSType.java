package org.jabref.model.database.shared;

import java.util.Locale;
import java.util.Optional;

/**
 * Enumerates all supported database systems (DBMS) by JabRef.
 */
public enum DBMSType {

    MYSQL("MySQL", "org.mariadb.jdbc.Driver", "jdbc:mariadb://%s:%d/%s", 3306),
    ORACLE("Oracle", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%d:%s", 1521),
    POSTGRESQL("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://%s:%d/%s", 5432);

    private final String type;
    private final String driverPath;
    private final String urlPattern;
    private final int defaultPort;

    private DBMSType(String type, String driverPath, String urlPattern, int defaultPort) {
        this.type = type;
        this.driverPath = driverPath;
        this.urlPattern = urlPattern;
        this.defaultPort = defaultPort;
    }

    @Override
    public String toString() {
        return this.type;
    }

    /**
     * @return Java Class path for establishing JDBC connection.
     */
    public String getDriverClassPath() throws Error {
        return this.driverPath;
    }

    /**
     * @return prepared connection URL for appropriate system.
     */
    public String getUrl(String host, int port, String database) {
        return String.format(urlPattern, host, port, database);
    }

    /**
     * Retrieves the port number dependent on the type of the database system.
     */
    public int getDefaultPort() {
        return this.defaultPort;
    }

    public static Optional<DBMSType> fromString(String typeName) {
        if (typeName == null) {
            return Optional.empty();
        } else {
            try {
                return Optional.of(Enum.valueOf(DBMSType.class, typeName.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException exception) {
                // IllegalArgumentException is thrown if "typename" is not covered by the Enum
                return Optional.empty();
            }
        }
    }
}
