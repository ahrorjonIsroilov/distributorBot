package ent;

import ent.handler.UpdateHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class Bot extends TelegramLongPollingBot {

    private final UpdateHandler handler;

    @Lazy
    public Bot(UpdateHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getBotUsername() {
        return "@distributorDazaBot";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    @Override
    public void onUpdateReceived(Update update) {
        handler.handle(update);
    }

    public void executeMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public String download(Document document, LocalDateTime day) {
        String fileId = document.getFileId();
        String fileName = document.getFileName();
        java.io.File directory = new java.io.File("downloads");
        if (!directory.exists()) directory.mkdir();
        String path = "downloads" + java.io.File.separator + day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " " + fileName;
        java.io.File[] files = directory.listFiles();
        if (files != null)
            for (java.io.File file : files)
                if (file.getName().startsWith(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))) file.delete();
        try {
            File file = execute(new GetFile(fileId));
            downloadFile(file, new java.io.File(path));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return path;
    }

    public void sendAudio(SendAudio message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendPhoto(SendPhoto photo) {
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendAnimation(SendAnimation message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendLocation(SendLocation location) {
        try {
            execute(location);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendVideo(SendVideo video) {
        try {
            execute(video);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendVoice(SendVoice video) {
        try {
            execute(video);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void send(SendDocument sendDocument) {
        try {
            this.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendSticker(SendSticker poll) {
        try {
            this.execute(poll);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
