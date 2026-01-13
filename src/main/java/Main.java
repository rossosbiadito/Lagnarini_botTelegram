import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {

        // Legge le credenziali dal file di configurazione
        String botToken = ConfigurationSingleton.getInstance().getProperty("BOT_TOKEN");
        String taddyApiKey = ConfigurationSingleton.getInstance().getProperty("TADDY_API_KEY");
        String taddyUserId = ConfigurationSingleton.getInstance().getProperty("TADDY_USER_ID");

        // Controlla se le chiavi esistono, altrimenti interrompe il programma
        if (botToken == null || taddyApiKey == null || taddyUserId == null) {
            System.err.println("Credenziali mancanti nel file config.properties!");
            System.err.println("Controlla di avere BOT_TOKEN, TADDY_API_KEY e TADDY_USER_ID");
            System.exit(-1);
        }

        try (
                // Avvia l'applicazione Telegram in modalità long polling
                TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()
        ) {
            // Registra il bot passandogli le credenziali
            botsApplication.registerBot(botToken, new PodcastBot_lagnarini(botToken, taddyApiKey, taddyUserId));

            System.out.println("Bot avviato");

            // Mantiene vivo il thread principale per non chiudere il bot
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Errore durante l'esecuzione del Bot:");
            e.printStackTrace();
        }
    }
}
