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
    private final Map<Long, podcast> lastSearchMap = new HashMap<>(); // Memorizza l'ultimo podcast cercato per ogni utente

    public PodcastBot_lagnarini(String botToken, String taddyApiKey, String taddyUserId) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.dbManager = new DatabaseManager();
        this.apiClient = new TaddyApiClient(taddyApiKey, taddyUserId);
    }

    @Override
    public void consume(Update update) {
        // Controllo base: deve esserci un messaggio di testo
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getUserName();

        if (text.startsWith("/")) {
            // Switch per gestire tutti i comandi
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
                case "/remove":
                    remove(chatId);
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
        sendText(chatId, "🎙️ *Benvenuto! Digita un podcast e scopri se il mondo lo ama… o se sei solo tu a salvarlo. * " +
                               "\n- Usa `/search <nome>` \n- Usa `/save` per i preferiti \n- Usa `/stats` per le tendenze \n- Usa `/myfavorites` per i tuoi salvati \n- Usa `/remove` per rimuovere i tuoi salvati");

    }

    // Cerca un podcast tramite API
    private void search(long chatId, String text) {
        String[] parts = text.split(" ", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            sendText(chatId, "Usa `/search <nome podcast>`");
            return;
        }

        String query = parts[1].trim();          // rimuove spazi all'inizio/fine della query
        List<podcast> results = apiClient.searchPodcasts(query);
        if (results.isEmpty()) {
            sendText(chatId, "Nessun risultato trovato.");
        } else {
            podcast p = results.get(0);
            lastSearchMap.put(chatId, p);
            sendPodcastPreview(chatId, p);
        }
    }

    // Salva l'ultimo podcast cercato nei preferiti
    private void save(long chatId) {
        podcast p = lastSearchMap.get(chatId);
        if (p == null) {
            sendText(chatId, "Cerca prima un podcast!");
            return;
        }

        if (dbManager.isFavoriteExists(chatId, p.getUuid())) {
            sendText(chatId, "Hai già salvato questo podcast!");
            return;
        }

        dbManager.addFavorite(chatId, p.getUuid(), p.getName());
        sendText(chatId, "✅ *" + p.getName() + "* salvato!");
    }

    // Rimuove l'ultimo podcast cercato dai preferiti
    private void remove(long chatId) {
        podcast last = lastSearchMap.get(chatId);
        if (last == null) {
            sendText(chatId, "Cerca prima un podcast da rimuovere!");
            return;
        }

        boolean removed = dbManager.removeFavorite(chatId, last.getUuid());     // tenta di rimuoverlo dai preferiti
        if (removed) {
            sendText(chatId, "❌ *" + last.getName() + "* rimosso dai preferiti!");
        } else {
            sendText(chatId, "Questo podcast non era nei tuoi preferiti.");
        }
    }


    // Mostra anteprima del podcast con descrizione e immagine
    private void sendPodcastPreview(long chatId, podcast p) {
        String caption = p.toString() + "\n\n💡 _Scrivi /save per salvarlo_";
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                telegramClient.execute(SendPhoto.builder()      // invia foto + testo
                        .chatId(chatId)
                        .photo(new InputFile(p.getImageUrl()))
                        .caption(caption)
                        .parseMode("Markdown")
                        .build());
            } catch (TelegramApiException e) {
                sendText(chatId, caption);      // se fallisce, invia solo il testo --> serve a gestire errori nell’invio della foto, ad esempio se l’URL è rotto o Telegram rifiuta l’immagine.
            }
        } else {
            sendText(chatId, caption);          // se no c'è l'immagine invia solo il testo --> serve a gestire il caso in cui non c’è proprio un URL dell’immagine
        }
    }

    // Invia un messaggio di testo semplice
    private void sendText(long chatId, String text) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")      // Imposta il formato del testo come Markdown, così puoi usare grassetto, corsivo, link ecc.
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
