package database;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:podcasts.db";

    public DatabaseManager() {
        // Costruttore: crea il database e le tabelle se non esistono
        try (Connection conn = DriverManager.getConnection(URL)) {
            Statement stmt = conn.createStatement();
            // Tabella utenti
            stmt.execute("CREATE TABLE IF NOT EXISTS users (chat_id INTEGER PRIMARY KEY, username TEXT)");
            // Tabella preferiti, collegata agli utenti
            stmt.execute("CREATE TABLE IF NOT EXISTS favorites (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "chat_id INTEGER, " +
                    "uuid TEXT, " +
                    "name TEXT, " +
                    "FOREIGN KEY(chat_id) REFERENCES users(chat_id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registerUser(long chatId, String username) {
        // Inserisce un utente nuovo, ignora se esiste già
        String sql = "INSERT OR IGNORE INTO users(chat_id, username) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             // pstmt è un oggetto PreparedStatement: permette di eseguire query SQL con parametri in modo sicuro
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addFavorite(long chatId, String uuid, String name) {
        // Aggiunge un podcast ai preferiti
        String sql = "INSERT INTO favorites(chat_id, uuid, name) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, uuid);
            pstmt.setString(3, name);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean isFavoriteExists(long chatId, String uuid) {
        // Controlla se un podcast è già tra i preferiti dell'utente
        String sql = "SELECT 1 FROM favorites WHERE chat_id = ? AND uuid = ?";
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, uuid);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // ritorna true se c'è almeno una riga
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean removeFavorite(long chatId, String uuid) {
        // Rimuove un podcast dai preferiti
        String sql = "DELETE FROM favorites WHERE chat_id = ? AND uuid = ?";
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            pstmt.setString(2, uuid);
            int deleted = pstmt.executeUpdate();
            return deleted > 0; // ritorna true se qualcosa è stato cancellato
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public String getGlobalStats() {
        // Ritorna i 3 podcast più salvati da tutti gli utenti
        String sql = "SELECT name, COUNT(*) as count FROM favorites GROUP BY name ORDER BY count DESC LIMIT 3";
        StringBuilder sb = new StringBuilder("📊 *Podcast più salvati:* \n");
        try (Connection conn = DriverManager.getConnection(URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            boolean data = false;
            while (rs.next()) {
                data = true;
                sb.append("- ").append(rs.getString("name")).append(" (").append(rs.getInt("count")).append(" fan)\n");
            }
            return data ? sb.toString() : "Ancora nessun preferito salvato.";
        } catch (SQLException e) { return "Errore statistiche."; }
    }

    public String getUserFavorites(long chatId) {
        // Ritorna tutti i podcast salvati da un utente
        String sql = "SELECT name FROM favorites WHERE chat_id = ?";
        StringBuilder sb = new StringBuilder("⭐️ *I tuoi preferiti:* \n");
        try (Connection conn = DriverManager.getConnection(URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, chatId);
            ResultSet rs = pstmt.executeQuery();
            boolean data = false;
            while (rs.next()) {
                data = true;
                sb.append("- ").append(rs.getString("name")).append("\n");
            }
            return data ? sb.toString() : "Non hai ancora salvato nulla.";
        } catch (SQLException e) { return "Errore recupero preferiti."; }
    }
}
