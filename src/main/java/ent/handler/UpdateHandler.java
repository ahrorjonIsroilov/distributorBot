package ent.handler;


import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Component
public class UpdateHandler implements IBaseHandler {
    private final Session sessions;
    private final MessageHandler messageHandler;
    private final CallBackHandler callbackHandler;
    private final ChatMemberHandler chatMemberHandler;

    public UpdateHandler(Session sessions, MessageHandler messageHandler, CallBackHandler callbackHandler, ChatMemberHandler chatMemberHandler) {
        this.sessions = sessions;
        this.messageHandler = messageHandler;
        this.callbackHandler = callbackHandler;
        this.chatMemberHandler = chatMemberHandler;
    }

    @Override
    public void handle(Update update) {
        monitoring(update);
        if (update.hasMessage()) messageHandler.handle(update);
        else if (update.hasCallbackQuery()) callbackHandler.handle(update);
        else chatMemberHandler.handle(update);
    }

    private void monitoring(Update update) {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        Runnable runnable = (() -> {
            AuthUser user = messageHandler.service.getByChatId(update.getMessage().getChatId());
            if (!Objects.equals(user.getUsername(), update.getMessage().getFrom().getUserName()) && user.getRegistered()) {
                user.setUsername(update.getMessage().getFrom().getUserName());
                messageHandler.service.save(user);
            }
        });
        pool.submit(runnable);
    }
}
