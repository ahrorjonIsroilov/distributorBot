package ent.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface IBaseHandler {
    void handle(Update update);
}
