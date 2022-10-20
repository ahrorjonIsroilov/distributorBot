package ent.handler;


import ent.Bot;
import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import ent.entity.Group;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import ent.entity.product.Product;
import ent.enums.Role;
import ent.enums.State;
import ent.service.Service;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Component
public class MessageHandler extends BaseMethods implements IBaseHandler {

    public final Session sessions;
    public final Service service;

    public MessageHandler(Bot bot, Session sessions, MarkupBoards markup, InlineBoards inline, Service service) {
        super(bot, markup, inline);
        this.sessions = sessions;
        this.service = service;
    }

    @Override
    public void handle(Update update) {
        prepare(update);
        String messageText = message.hasText() ? message.getText() : "";
        if (service.isRegistered(chatId)) {
            State state = sessions.getByChatId(chatId).getState();
            if (!sessions.isBlocked(chatId)) {
                switch (messageText) {
                    case "/start" -> {
                        start();
                        return;
                    }
                    case "Bekor qilish ❌" -> {
                        cancel();
                        return;
                    }
                    case "Jadvalni yuklash 📝" -> {
                        loadForm();
                        return;
                    }
                    case "Tarix 📁" -> {
                        chooseDate();
                        return;
                    }
                    case "Taqsimotchilar 🧢" -> {
                        distributors();
                        return;
                    }
                    case "Guruhlar 👥" -> {
                        groups();
                        return;
                    }
                    case "/admins" -> {
                        admins();
                        return;
                    }
                    case "/template" -> {
                        sendTemplate();
                        return;
                    }
                }
                switch (state) {
                    case UPLOAD_FORM -> uploadForm(update);
                    case EDIT_PRODUCT -> editProduct(update);
                    case LOAD_HISTORY -> loadFileFromDate(update);
                    case ADD_DIS -> addDistributor(update);
                    case ADD_ADMIN -> addAdmin(update);
                }
            } else sendMessage(chatId, "Bloklangansiz!", new ReplyKeyboardRemove(true));
        } else if (!hasUsername(update))
            sendMessage(chatId, "<b>Kechirasiz! Botni faqat telegram foydalanuvchi nomiga ega insonlar ishlatishlari mumkin 😕</b>");
        else if (service.existsByUsernameAndRegisteredFalse(user.getUserName()))
            register(service.getByUsername(user.getUserName()).getRole());
    }

    private void loadFileFromDate(Update update) {
        String day;
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                if (messageHasText(update)) {
                    day = message.getText();
                    if (validDateFormat(day)) {
                        findAndSendFiles(day);
                    } else
                        sendMessage(chatId, "<i>Kiritilgan sana to'g'ri formatda emas iltimos quyidagi formatda kiriting:</i>\n<b>31-12-2022</b>");
                } else
                    sendMessage(chatId, "<b>Sanani kiriting:</b>");
            }
        }
    }

    private void findAndSendFiles(String day) {
        File downloads = new File("downloads");
        File[] files = downloads.listFiles();
        if (files != null) {
            if (fileFound(files, day)) {
                for (File file : files) {
                    if (file.getName().startsWith(day)) {
                        SendDocument document = new SendDocument();
                        document.setParseMode("HTML");
                        document.setDocument(new InputFile(file));
                        sessions.setState(State.DEFAULT, chatId);
                        document.setChatId(chatId);
                        bot.send(document);
                        break;
                    }
                }
            } else {
                sessions.setState(State.DEFAULT, chatId);
                sendMessage(chatId, "<i>Ushbu sana uchun fayllar topilmadi</i>", markup.adminPanel());
            }
        }
    }

    private boolean fileFound(File[] files, String day) {
        for (File file : files) {
            if (file.getName().startsWith(day)) return true;
        }
        return false;
    }

    private boolean validDateFormat(String day) {
        return Pattern.compile("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[012])-((19|2[0-9])[0-9]{2})$").matcher(day).matches();
    }

    private void chooseDate() {
        sendMessage(chatId, "Sanani quyidagi formatda kiriting:\n<b>31-12-2022</b>", markup.cancel());
        sessions.setState(State.LOAD_HISTORY, chatId);
    }

    private void sendTemplate() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                SendDocument document = new SendDocument();
                document.setChatId(chatId);
                document.setDocument(new InputFile(new File("template.xlsx")));
                document.setCaption("<b>Botga ushbu namunadan foydalanib jadvalni yuboring</b>");
                document.setParseMode("HTML");
                bot.send(document);
            }
            case DISTRIBUTOR -> sendMessage(chatId, "<i>Ushbu buyruqni faqat adminlar ishlata oladi</i>");
            default -> {
            }
        }
    }

    private void editProduct(Update update) {
        if (messageHasText(update)) {
            String newCount = message.getText();
            if (validNumber(newCount)) {
                int count = Integer.parseInt(newCount);
                String id = sessions.getTempString(chatId);
                if (count < 0) {
                    sendMessage(chatId, "Kiritiladigan miqdor 0 dan kichik bo'lmasligi kerak");
                    return;
                }
                Product product = service.getProduct(id);
                if (product.getTotalCount() == count) {
                    sessions.setState(State.DEFAULT, chatId);
                    List<Product> productList = service.getAllProducts(sessions.getDay(chatId), sessions.getPage(chatId));
                    sendMessage(chatId, "<b>Mahsulotlar</b>", inline.productList(productList, sessions.getPage(chatId)));
                    return;
                }
                if (service.editProduct(id, count)) {
                    sendMessage(chatId, "<b>O'zgarishlar saqlandi!</b> \n<i>Hammasi tayyor bo'lsa</i> <b>[Tayyor ✅]</b><i> tugmasini bosing</i>", inline.acceptOrContinue());
                    sessions.setState(State.DEFAULT, chatId);
                } else sendMessage(chatId, "<i>Yangi miqdor eskisidan ko'p bo'lmasligi kerak</i>");
            } else sendMessage(chatId, "<i>To'g'ri qiymat kiriting!</i>");
        }
    }

    private boolean validNumber(String newCount) {
        try {
            Integer.parseInt(newCount);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void register(Role role) {
        service.register(chatId, user.getUserName(), user.getFirstName());
        sessions.setSession(chatId);
        start();
    }

    private void addAdmin(Update update) {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                addUser(update, Role.ADMIN, "Admin");
            }
        }
    }

    private void addDistributor(Update update) {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> addUser(update, Role.DISTRIBUTOR, "Taqsimotchi");
        }
    }

    private void admins() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                sessions.setPageZero(chatId);
                List<AuthUser> admins = service.getAllAdmins(sessions.getByChatId(chatId).getPage());
                if (admins.size() < 1) {
                    sendMessage(chatId, "Adminlar mavjud emas", inline.addAdmin());
                    return;
                }
                sendMessage(chatId, "Adminlar", inline.userList(admins, sessions.getByChatId(chatId).getPage(), Role.ADMIN));
            }
            default -> {
            }
        }
    }

    private void distributors() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                sessions.setPageZero(chatId);
                List<AuthUser> distributors = service.getAllDistributors(sessions.getByChatId(chatId).getPage());
                if (distributors.size() < 1) {
                    sendMessage(chatId, "Taqsimotchilar mavjud emas", inline.addDistributor());
                    return;
                }
                sendMessage(chatId, "Taqsimotchilar", inline.userList(distributors, sessions.getByChatId(chatId).getPage(), Role.DISTRIBUTOR));
            }
            default -> {
            }
        }
    }

    private void groups() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                sessions.setPageZero(chatId);
                List<Group> groups = service.getGroups(sessions.getByChatId(chatId).getPage());
                if (groups.size() < 1) {
                    sendMessage(chatId, "Guruhlar topilmadi. Botni guruhga qo'shish uchun pastdagi tugmani bosing 👇", inline.addToGroup());
                    return;
                }
                sendMessage(chatId, "Guruhlar ro'yxati\n[Tanlangan guruhlar qatoriga qo'shish yoki o'chirish uchun guruh ustiga bosing]", inline.groupList(groups, sessions.getByChatId(chatId).getPage()));
            }
            default -> {
            }
        }
    }

    private void uploadForm(Update update) {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                if (messageHasDocument(update)) {
                    Document doc = message.getDocument();
                    String path = bot.download(doc, sessions.getDay(chatId));
                    sessions.setState(State.DEFAULT, chatId);
                    service.readData(path, sessions.getByChatId(chatId).getDate(), update, bot, markup);
                    return;
                }
                sessions.setState(State.DEFAULT, chatId);
                sendMessage(chatId, "Iltimos excel fayl yuboring", markup.adminPanel());
            }
        }
    }

    private String sendUsernames(ArrayList<String> usernames) {
        StringBuilder builder = new StringBuilder();
        for (String username : usernames) {
            builder.append(username).append("\n");
        }
        return builder.toString();
    }

    private void loadForm() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> sendMessage(chatId, "<b>Qaysi kun uchun yuklaysiz</b>", inline.dayButtons(sessions.getRole(chatId)));
            default -> {
            }
        }
    }

    private void cancel() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> {
                sessions.setState(State.DEFAULT, chatId);
                sendMessage(chatId, "Bekor qilindi", markup.adminPanel());
            }
            case DISTRIBUTOR -> {
                sessions.setState(State.DEFAULT, chatId);
                sendMessage(chatId, "Bekor qilindi", markup.storekeeperPanel());
            }
            default -> {
            }
        }
    }

    private void start() {
        switch (sessions.getRole(chatId)) {
            case ADMIN, OWNER -> sendMessage(chatId, "<b>Assalomu alaykum</b>", markup.adminPanel());
            case DISTRIBUTOR -> sendMessage(chatId, "<b>Assalomu alaykum</b>", inline.storeKeeperPanel());
            default -> {
            }
        }
    }

    private void addUser(Update update, Role role, String roleName) {
        if (messageHasText(update)) {
            String username = message.getText();
            username = username.startsWith("@") ? username.substring(1) : username;
            if (!service.existsByUsername(username)) {
                AuthUser distributor = AuthUser.builder()
                        .blocked(false)
                        .registered(false)
                        .phoneNumber("NONE")
                        .state(State.DEFAULT)
                        .page(0)
                        .role(role)
                        .username(username)
                        .build();
                service.save(distributor);
                sessions.setState(State.DEFAULT, chatId);
                sendMessage(chatId, roleName + " qo'shildi!", markup.adminPanel());
            } else {
                sendMessage(chatId, "Bunday nom bilan foydalanuvchi mavjud!", markup.adminPanel());
                sessions.setState(State.DEFAULT, chatId);
            }
        } else sendMessage(chatId, "Iltimos to'g'ri ma'lumot kiriting!");
    }
}
