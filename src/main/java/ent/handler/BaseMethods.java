package ent.handler;


import ent.Bot;
import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.toIntExact;

@Component
@RequiredArgsConstructor
public class BaseMethods {
    protected final Bot bot;
    protected final MarkupBoards markup;
    protected final InlineBoards inline;
    public Message message;
    public CallbackQuery callbackQuery;
    public String mText;
    public User user;
    public Long chatId;

    public void prepare(Update update) {
        if (update.hasCallbackQuery()) message = update.getCallbackQuery().getMessage();
        else message = update.getMessage();
        callbackQuery = update.getCallbackQuery();
        mText = message.getText();
        chatId = message.getChatId();
        user = message.getFrom();
    }

    public SendMessage msgObject(long chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId + "", text);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

    public EditMessageText eMsgObject(Update update, InlineKeyboardMarkup markup) {
        long messageId;
        long chatId;
        String messageText;
        List<MessageEntity> entities;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
            entities = update.getCallbackQuery().getMessage().getEntities();
            messageText = update.getCallbackQuery().getMessage().getText();
        } else {
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();
            messageText = update.getMessage().getText();
            entities = update.getMessage().getEntities();
        }
        EditMessageText sendMessage = new EditMessageText();
        sendMessage.setText(messageText);
        sendMessage.setEntities(entities);
        sendMessage.setMessageId(toIntExact(messageId));
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(markup);
        return sendMessage;
    }

    public EditMessageText eMsgObject(Update update, InlineKeyboardMarkup markup, String text) {
        long messageId;
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
        } else {
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();
        }
        EditMessageText ed = new EditMessageText();
        ed.setText(text);
        ed.setMessageId(toIntExact(messageId));
        ed.setChatId(chatId);
        ed.enableHtml(true);
        ed.setReplyMarkup(markup);
        return ed;
    }

    public AnswerCallbackQuery popupMessage(String text, String callbackQueryId) {
        AnswerCallbackQuery query = new AnswerCallbackQuery();
        query.setText(text);
        query.setCallbackQueryId(callbackQueryId);
        try {
            bot.execute(query);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return query;
    }

    public EditMessageText eMsgObject(Update update, String text) {
        long messageId;
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            messageId = update.getCallbackQuery().getMessage().getMessageId();
        } else {
            chatId = update.getMessage().getChatId();
            messageId = update.getMessage().getMessageId();
        }
        EditMessageText ed = new EditMessageText();
        ed.setText(text);
        ed.setMessageId(toIntExact(messageId));
        ed.setChatId(chatId);
        ed.enableHtml(true);
        return ed;
    }

    public SendPhoto ePhoto(Long chatId, String caption, String path) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new InputFile(new File(path)));
        sendPhoto.setChatId(chatId.toString());
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("HTML");
        return sendPhoto;
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = msgObject(chatId, text);
        bot.executeMessage(sendMessage);
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboardMarkup markup) {
        SendMessage sendMessage = msgObject(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.executeMessage(sendMessage);
    }

    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = msgObject(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.executeMessage(sendMessage);
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboardRemove markup) {
        SendMessage sendMessage = msgObject(chatId, text);
        sendMessage.setReplyMarkup(markup);
        bot.executeMessage(sendMessage);
    }

    protected boolean messageHasText(Update update) {
        return update.getMessage().hasText();
    }

    protected final Boolean validDocument(Document document) {
        return document.getFileName().endsWith(".xls") || document.getFileName().endsWith(".xlsx");
    }

    protected boolean messageHasDocument(Update update) {
        if (message.hasDocument()) {
            Document d = message.getDocument();
            return validDocument(d);
        }
        return false;
    }

    protected boolean messageTextEquals(Update update, String text) {
        return update.getMessage().hasText() && update.getMessage().getText().equals(text);
    }

    protected boolean hasUsername(Update update) {
        return Objects.nonNull(update.getMessage().getFrom().getUserName());
    }

    public String beautyPhone(String phoneNumber) {
        String countryCode = phoneNumber.substring(3, 5);
        String first = phoneNumber.substring(5, 8);
        String second = phoneNumber.substring(8, 10);
        String third = phoneNumber.substring(10);
        return "(" + countryCode + ")" +
            first + " " +
            second + " " +
            third;
    }
}
