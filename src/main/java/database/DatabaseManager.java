package database;

import java.sql.*;
import api.podcast;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager() {
        try {
            String url = "jdbc:sqlite:data/podcast_bot.db";
            connection = DriverManager.getConnection(url);
            setupDatabase();
        } catch (SQLException e) {
            System.err.println("Errore connessione DB: " + e.getMessage());
        }
    }

    private void setupDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "chat_id INTEGER PRIMARY KEY, " +
                    "username TEXT, " +
                    "last_search TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS saved_podcasts (" +
                    "user_id INTEGER, " +
                    "podcast_uuid TEXT, " +
                    "podcast_name TEXT, " +
                    "saved_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (user_id, podcast_uuid))");

            stmt.execute("CREATE TABLE IF NOT EXISTS stats (" +
                    "user_id INTEGER PRIMARY KEY, " +
                    "previews_heard INTEGER DEFAULT 0, " +
                    "total_seconds INTEGER DEFAULT 0)");
        }
    }

    public void addFavorite(long chatId, podcast p) {
        String sql = "INSERT OR IGNORE INTO saved_podcasts(user_id, podcast_uuid, podcast_name) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, p.getUuid());
            pstmt.setString(3, p.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void incrementStats(long chatId, int seconds) {
        String sql = "INSERT INTO stats(user_id, previews_heard, total_seconds) VALUES(?, 1, ?) " +
                "ON CONFLICT(user_id) DO UPDATE SET " +
                "previews_heard = previews_heard + 1, " +
                "total_seconds = total_seconds + ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setInt(2, seconds);
            pstmt.setInt(3, seconds);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}