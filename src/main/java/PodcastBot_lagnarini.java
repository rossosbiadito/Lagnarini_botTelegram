import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
public class PodcastBot_lagnarini implements LongPollingSingleThreadUpdateConsumer {
        private TelegramClient telegramClient;

    public PodcastBot_lagnarini(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (text.equals("/start")) {
            sendText(chatId,
                    "Ciao!\n" +
                            "Usa /search per cercare un podcast.\n" +
                            "Esempio: /search tecnologia");
        }
        else if (text.startsWith("/search")) {

            String[] parts = text.split(" ", 2);

            if (parts.length < 2) {
                sendText(chatId, "Devi scrivere: /search argomento");
                return;
            }

            String query = parts[1];
            sendText(chatId, "Hai cercato: " + query);
        }

        else if (text.equals("/next")) {
            sendText(chatId, "Mostro il prossimo podcast");
        }
    }
    private void sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}

