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
            // fare switch case e per ogni caso chiamo un metodo esterno
            if (update.hasMessage() && update.getMessage().hasText()) {
                String message_text = update.getMessage().getText();
                long chat_id = update.getMessage().getChatId();
                if(update.getMessage().getText().equals("/pizza")){
                    message_text="Pizzeria mamma mia come posso essere d'aiuto";
                }
                SendMessage message = SendMessage
                        .builder()
                        .chatId(chat_id)
                        .text(message_text)
                        .build();
                try{
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

