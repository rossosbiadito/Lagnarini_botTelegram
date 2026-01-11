import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {

        String botToken = ConfigurationSingleton.getInstance().getProperty("BOT_TOKEN");

        try (TelegramBotsLongPollingApplication botsApplication =
                     new TelegramBotsLongPollingApplication()) {

            botsApplication.registerBot(botToken, new PodcastBot_lagnarini(botToken));
            System.out.println("Bot che si accende");

            // Mantiene vivo il thread principale
            Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
