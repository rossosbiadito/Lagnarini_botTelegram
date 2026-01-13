import api.TaddyApiClient;
import api.podcast;
import database.DatabaseManager;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodcastBot_lagnarini implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final TaddyApiClient apiClient;
    private final DatabaseManager dbManager;
    private final Map<Long, podcast> lastSearchMap = new HashMap<>();

    public PodcastBot_lagnarini(String botToken, String taddyApiKey, String taddyUserId) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.dbManager = new DatabaseManager();
        this.apiClient = new TaddyApiClient(taddyApiKey, taddyUserId);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();

        if (text.startsWith("/")) {
            switch (text.split(" ")[0]) {
                case "/start":
                    start(chatId, username);
                    break;

                case "/search":
                    search(chatId, text);
                    break;

                case "/save":
                    save(chatId);
                    break;

                case "/stats":
                    sendText(chatId, dbManager.getGlobalStats());
                    break;

                case "/myfavorites":
                    sendText(chatId, dbManager.getUserFavorites(chatId));
                    break;

                default:
                    sendText(chatId, "Comando non riconosciuto.");
            }
        }
    }

    private void start(long chatId, String username) {
        dbManager.registerUser(chatId, username != null ? username : "User");
        sendText(chatId, "🎙️ *Benvenuto! * \n- Usa `/search <nome>` \n- Usa `/save` per i preferiti \n- Usa `/stats` per le tendenze \n- Usa `/myfavorites` per i tuoi salvati");
    }

    private void search(long chatId, String text) {
        String[] parts = text.split(" ", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            sendText(chatId, "⚠️ Usa `/search <nome podcast>`");
            return;
        }

        String query = parts[1].trim();
        List<podcast> results = apiClient.searchPodcasts(query);
        if (results.isEmpty()) {
            sendText(chatId, "❌ Nessun risultato trovato.");
        } else {
            podcast p = results.get(0);
            lastSearchMap.put(chatId, p);
            sendPodcastPreview(chatId, p);
        }
    }

    private void save(long chatId) {
        podcast p = lastSearchMap.get(chatId);
        if (p == null) {
            sendText(chatId, "⚠️ Cerca prima un podcast!");
            return;
        }

        if (dbManager.isFavoriteExists(chatId, p.getUuid())) {
            sendText(chatId, "⚠️ Hai già salvato questo podcast!");
            return;
        }

        dbManager.addFavorite(chatId, p.getUuid(), p.getName());
        sendText(chatId, "✅ *" + p.getName() + "* salvato!");
    }

    private void sendPodcastPreview(long chatId, podcast p) {
        String caption = p.toString() + "\n\n💡 _Scrivi /save per salvarlo_";
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                telegramClient.execute(SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(p.getImageUrl()))
                        .caption(caption)
                        .parseMode("Markdown")
                        .build());
            } catch (TelegramApiException e) {
                sendText(chatId, caption);
            }
        } else {
            sendText(chatId, caption);
        }
    }

    private void sendText(long chatId, String text) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
