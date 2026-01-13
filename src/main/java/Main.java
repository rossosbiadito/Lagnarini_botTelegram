import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        String botToken = ConfigurationSingleton.getInstance().getProperty("BOT_TOKEN");
        String taddyApiKey = ConfigurationSingleton.getInstance().getProperty("TADDY_API_KEY");
        String taddyUserId = ConfigurationSingleton.getInstance().getProperty("TADDY_USER_ID");

        if (botToken == null || taddyApiKey == null || taddyUserId == null) {
            System.err.println("Credenziali mancanti nel file config.properties!");
            System.err.println("Controlla di avere BOT_TOKEN, TADDY_API_KEY e TADDY_USER_ID");
            System.exit(-1);
        }

        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new PodcastBot_lagnarini(botToken, taddyApiKey, taddyUserId));
            System.out.println("Bot avviato");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Errore durante l'esecuzione del Bot:");
            e.printStackTrace();
        }
    }
}
