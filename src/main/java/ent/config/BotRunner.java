package ent.config;

import ent.Bot;
import ent.handler.UpdateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotRunner {
    private final UpdateHandler handler;

    public BotRunner(UpdateHandler handler) {
        this.handler = handler;
    }

    @Bean
    public void main() throws InterruptedException {
        TelegramBotsApi api;
        try {
            api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new Bot(handler));
            System.out.println("connected");
        } catch (TelegramApiException e) {
            System.out.println("Not connected");
            Thread.sleep(10000);
            main();
        }

    }

}
