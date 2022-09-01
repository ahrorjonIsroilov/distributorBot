package ent.button;


import ent.enums.Emojis;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collections;
import java.util.List;

@Component
public class MarkupBoards {

    private final ReplyKeyboardMarkup board = new ReplyKeyboardMarkup();

    public ReplyKeyboardMarkup phoneButton() {
        KeyboardButton phoneContact = new KeyboardButton("Share contact 📞");
        phoneContact.setRequestContact(true);
        KeyboardRow row1 = new KeyboardRow();
        row1.add(phoneContact);
        board.setKeyboard(Collections.singletonList(row1));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup storekeeperPanel() {
        KeyboardButton doChange = new KeyboardButton("O'zgarish bor " + Emojis.INTERROBANG.emoji());
        KeyboardRow row = new KeyboardRow();
        row.add(doChange);
        board.setKeyboard(Collections.singletonList(row));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup cancel() {
        KeyboardButton cancel = new KeyboardButton("Bekor qilish ❌");
        KeyboardRow row = new KeyboardRow();
        row.add(cancel);
        board.setKeyboard(Collections.singletonList(row));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup back() {
        KeyboardButton back = new KeyboardButton("Orqaga 🔙");
        KeyboardRow row = new KeyboardRow();
        row.add(back);
        board.setKeyboard(Collections.singletonList(row));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }

    public ReplyKeyboardMarkup adminPanel() {
        KeyboardButton groups = new KeyboardButton("Guruhlar 👥");
        KeyboardButton products = new KeyboardButton("Mahsulotlar 👥");
        KeyboardButton distributors = new KeyboardButton("Taqsimotchilar 🧢");
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        row1.addAll(List.of(groups, products));
        row2.add(distributors);
        board.setKeyboard(List.of(row1, row2));
        board.setResizeKeyboard(true);
        board.setSelective(true);
        return board;
    }
}
