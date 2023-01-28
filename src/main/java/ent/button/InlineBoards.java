package ent.button;

import ent.entity.Deliver;
import ent.entity.Group;
import ent.entity.auth.AuthUser;
import ent.entity.product.Product;
import ent.enums.Exclusions;
import ent.enums.Role;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InlineBoards {
    private final InlineKeyboardMarkup board = new InlineKeyboardMarkup();

    public InlineKeyboardMarkup addToGroup() {
        InlineKeyboardButton addButton = new InlineKeyboardButton("Guruhga qo'shish ➕");
        addButton.setUrl("https://telegram.me/distributorDazaBot?startgroup=true");
        board.setKeyboard(Collections.singletonList(getRow(addButton)));
        return board;
    }

    public InlineKeyboardMarkup dayButtons(Role role) {
        InlineKeyboardButton today = new InlineKeyboardButton();
        today.setText("Bugun");
        InlineKeyboardButton home = new InlineKeyboardButton();
        home.setText("Bosh menyu");
        home.setCallbackData("home#" + role.getCode());
        boolean checkRole = role.equals(Role.ADMIN) || role.equals(Role.OWNER);
        today.setCallbackData(checkRole ? "addT" : "editT");
        InlineKeyboardButton tomorrow = new InlineKeyboardButton();
        tomorrow.setText("Ertaga");
        tomorrow.setCallbackData(checkRole ? "addTw" : "editTw");
        if (role.equals(Role.DISTRIBUTOR))
            board.setKeyboard(List.of(getRow(today, tomorrow), getRow(home)));
        else
            board.setKeyboard(List.of(getRow(today, tomorrow)));
        return board;
    }

    public InlineKeyboardMarkup addDistributor() {
        InlineKeyboardButton addDistributor = new InlineKeyboardButton("Taqsimotchi qo'shish ➕");
        addDistributor.setCallbackData("addDis");
        board.setKeyboard(Collections.singletonList(getRow(addDistributor)));
        return board;
    }

    public InlineKeyboardMarkup addAdmin() {
        InlineKeyboardButton addDistributor = new InlineKeyboardButton("Admin qo'shish ➕");
        addDistributor.setCallbackData("addAdmin");
        board.setKeyboard(Collections.singletonList(getRow(addDistributor)));
        return board;
    }


    InlineKeyboardButton fixedButton(String buttonName, String url) {
        InlineKeyboardButton fixedButton = new InlineKeyboardButton(buttonName);
        fixedButton.setUrl(url);
        return fixedButton;
    }

    InlineKeyboardButton fixedButton(String buttonName, String callbackData, Integer nul) {
        InlineKeyboardButton fixedButton = new InlineKeyboardButton(buttonName);
        fixedButton.setCallbackData(callbackData);
        return fixedButton;
    }

    public InlineKeyboardMarkup yesNo() {
        InlineKeyboardButton send = new InlineKeyboardButton("Confirm ✅");
        send.setCallbackData("accept");
        InlineKeyboardButton decline = new InlineKeyboardButton("Cancel ❌");
        decline.setCallbackData("decline");
        board.setKeyboard(Collections.singletonList(getRow(send, decline)));
        return board;
    }

    public InlineKeyboardMarkup userButton(AuthUser user) {
        InlineKeyboardButton blockedState = new InlineKeyboardButton();
        InlineKeyboardButton remove = new InlineKeyboardButton();
        remove.setText("O'chirish ➖");
        remove.setCallbackData("remove#" + user.getId() + "#" + user.getRole().getCode());
        blockedState.setCallbackData("block#" + user.getId());
        blockedState.setText(user.getBlocked() ? "Blokdan chiqarish 🔓" : "Bloklash 🚫");
        board.setKeyboard(List.of(getRow(blockedState, remove), getRow(close())));
        return board;
    }

    public InlineKeyboardMarkup userList(List<AuthUser> users, Integer page, Role role) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (AuthUser user : users) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            if (user.getRegistered()) {
                btn.setText(!user.getBlocked() ? user.getUsername() + " ✅" : user.getUsername() + " 🚫");
                btn.setCallbackData("user#" + user.getId());
            } else {
                btn.setText(user.getUsername() + " [unregistered]");
                btn.setCallbackData("user#" + user.getId());
            }
            buttons.add(btn);
        }
        buttons.add(fixedButton("Qo'shish ➕", role.equals(Role.ADMIN) ? "addAdmin" : "addDis", null));
        board.setKeyboard(prepareButtons(buttons, role.equals(Role.ADMIN) ? ".admin" : ".dis", page));
        return board;
    }

    public InlineKeyboardMarkup groupList(List<Group> groups, Integer page) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Group group : groups) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            if (group.getAccepted()) btn.setText(group.getTitle() + " ✅");
            else btn.setText(group.getTitle());
            btn.setCallbackData("group#" + group.getGroupId());
            buttons.add(btn);
        }
        board.setKeyboard(prepareButtons(buttons, ".gr", page));
        return board;
    }

    public InlineKeyboardMarkup productList(List<Product> products, Integer page) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton ready = new InlineKeyboardButton("Tayyor ✅");
        ready.setCallbackData("ready");
        for (Product product : products) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(Math.round((long) product.getTotalCount()) + " | " + product.getName());
            btn.setCallbackData("product#" + product.getId());
            buttons.add(btn);
        }
        List<List<InlineKeyboardButton>> lists = prepareButtons(buttons, ".product", page);
        lists.add(getRow(ready));
        board.setKeyboard(lists);
        return board;
    }

    public InlineKeyboardMarkup productListForExclusion(String exclusionName, List<Product> products, Integer page) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton home = new InlineKeyboardButton();
        home.setText("Bosh menyu");

        InlineKeyboardButton ready = new InlineKeyboardButton("Tayyor ✅");
        ready.setCallbackData("ready");
        for (Product product : products) {
            for (Deliver deliver : product.getDelivers()) {
                if (deliver.getUsername().equalsIgnoreCase(exclusionName)) {
                    InlineKeyboardButton btn = new InlineKeyboardButton();
                    btn.setText(Math.round((long) product.getTotalCount()) + "-" + Math.round((long) deliver.getProductCount()) + " | " + product.getName());
                    btn.setCallbackData("prod-exclusion#" + product.getId());
                    buttons.add(btn);
                }
            }
        }
        List<List<InlineKeyboardButton>> lists = prepareButtons(buttons, ".prod-exclusion", page);
        board.setKeyboard(lists);
        return board;
    }

    public InlineKeyboardMarkup storeKeeperPanel() {
        InlineKeyboardButton doChange = new InlineKeyboardButton("O'zgarish bor ⁉️");
        doChange.setCallbackData("dochange");
        board.setKeyboard(List.of(getRow(doChange)));
        return board;
    }

    private List<List<InlineKeyboardButton>> prepareButtons(List<InlineKeyboardButton> input, String mark, Integer userPage) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int i = 0; i < input.size(); i += 2) {
            if (i + 1 < input.size()) {
                buttons.add(getRow(input.get(i), input.get(i + 1)));
            } else {
                buttons.add(getRow(input.get(i)));
            }
        }
        if (input.size() < 10 && userPage > 0) buttons.add(prevX(mark));
        else if (userPage < 1) buttons.add(nextX(mark));
        else buttons.add(prevXNext(mark));
        if (mark.equals(".gr"))
            buttons.add(getRow(fixedButton("Yangi guruhga qo'shish", "https://telegram.me/distributorDazaBot?startgroup=true")));
        return buttons;
    }

    public List<InlineKeyboardButton> prevX(String mark) {
        InlineKeyboardButton backToExclusion = new InlineKeyboardButton("Tayyor ✅");
        backToExclusion.setCallbackData("back-to-exclusion");
        InlineKeyboardButton previous = new InlineKeyboardButton("⬅️");
        previous.setCallbackData("previous" + mark);
        if (mark.equals(".prod-exclusion")) return new ArrayList<>(getRow(previous));
        return new ArrayList<>(getRow(previous, home()));
    }

    public List<InlineKeyboardButton> nextX(String mark) {
        InlineKeyboardButton backToExclusion = new InlineKeyboardButton("Tayyor ✅");
        backToExclusion.setCallbackData("back-to-exclusion");
        InlineKeyboardButton next = new InlineKeyboardButton("➡️");
        next.setCallbackData("next" + mark);
        if (mark.equals(".prod-exclusion")) return new ArrayList<>(getRow(backToExclusion, next));
        return new ArrayList<>(getRow(home(), next));
    }

    public InlineKeyboardButton close() {
        InlineKeyboardButton close = new InlineKeyboardButton("✖️");
        close.setCallbackData("close");
        return close;
    }

    public InlineKeyboardButton home() {
        InlineKeyboardButton home = new InlineKeyboardButton();
        home.setText("Bosh menyu 🏡");
        home.setCallbackData("home#" + "storekeeper");
        return home;
    }

    public List<InlineKeyboardButton> prevXNext(String mark) {
        InlineKeyboardButton backToExclusion = new InlineKeyboardButton("Tayyor ✅");
        backToExclusion.setCallbackData("back-to-exclusion");
        InlineKeyboardButton previous = new InlineKeyboardButton("⬅️");
        previous.setCallbackData("previous" + mark);
        InlineKeyboardButton next = new InlineKeyboardButton("➡️");
        next.setCallbackData("next" + mark);
        if (mark.equals(".prod-exclusion")) return new ArrayList<>(getRow(previous, next));
        return new ArrayList<>(getRow(previous, home(), next));
    }

    private List<InlineKeyboardButton> getRow(InlineKeyboardButton... buttons) {
        return Arrays.stream(buttons).collect(Collectors.toList());
    }

    public InlineKeyboardMarkup showExclusions() {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        InlineKeyboardButton continueB = new InlineKeyboardButton("Davom etish ✅");
        continueB.setCallbackData("ready-to-others");
        for (Exclusions exclusion : Exclusions.values()) {
            InlineKeyboardButton exButton = new InlineKeyboardButton(exclusion.getVal());
            exButton.setCallbackData("exclusion#" + exclusion.getVal());
            buttons.add(exButton);
        }
        board.setKeyboard(List.of(buttons, getRow(continueB)));
        return board;
    }
}
