import database.DatabaseManager;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import api.TaddyApiClient;
import api.podcast;
import java.util.List;

public class PodcastBot_lagnarini implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private TaddyApiClient apiClient;
    private List<podcast> currentResults;
    private int currentIndex = 0;
    // Dentro PodcastBot_lagnarini
    private DatabaseManager db;

    public PodcastBot_lagnarini(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);

        String apiKey = ConfigurationSingleton.getInstance().getProperty("TADDY_API_KEY");
        String userId = ConfigurationSingleton.getInstance().getProperty("TADDY_USER_ID");

        this.db = new DatabaseManager(); // Inizializza il database
        this.apiClient = new TaddyApiClient(apiKey, userId);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (text.equals("/start")) {
            sendText(chatId, "🎙️ *Benvenuto nel Bot Podcast Taddy!*\n\nUsa /search <argomento> per iniziare.");
        }
        else if (text.startsWith("/search")) {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                sendText(chatId, "Devi scrivere: /search <argomento>");
                return;
            }

            String query = parts[1];
            sendText(chatId, "🔍 Sto cercando podcast su: *" + query + "*...");
            currentResults = apiClient.searchPodcasts(query);

            if (currentResults == null || currentResults.isEmpty()) {
                sendText(chatId, "Nessun podcast trovato per: " + query);
            } else {
                currentIndex = 0;
                sendPodcastPreview(chatId, currentResults.get(currentIndex));
            }
        }
        else if (text.equals("/next")) {
            if (currentResults == null || currentResults.isEmpty()) {
                sendText(chatId, "Fai prima una ricerca con /search");
                return;
            }
            currentIndex = (currentIndex + 1) % currentResults.size();
            sendPodcastPreview(chatId, currentResults.get(currentIndex));
        }
    }

    // ✅ SEMPLIFICATO - solo testo
    private void sendPodcastPreview(long chatId, podcast p) {
        sendText(chatId, p.toString());
        sendText(chatId, "Premi /next per il prossimo.");
    }

    private void sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("Markdown")
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}