package ru.akirakozov.sd.refactoring.sql;

import java.io.IOException;
import java.sql.*;
import java.util.function.Consumer;

public class SqlWorker {
    public static void update(final String query) {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            try (Statement stmt = c.createStatement()) {
                stmt.executeUpdate(query);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface QueryConsumer<T> {
        void accept(T t) throws IOException, SQLException;
    }

    public static void query(final String query, final QueryConsumer<ResultSet> consumer) throws IOException {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            consumer.accept(c.createStatement().executeQuery(query));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
