import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
public class PodcastBot_lagnarini implements LongPollingSingleThreadUpdateConsumer {
        private TelegramClient telegramClient = new OkHttpTelegramClient("8238419476:AAG8Ahu4-5sa4jnb1fDraL27U8jy7VEtXy8");

        @Override
        public void consume(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String message_text = update.getMessage().getChatId();
                long chat_id = update.getMessage().getChatId();

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

