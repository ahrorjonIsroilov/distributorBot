package ent.handler;

import ent.Bot;
import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import ent.entity.Deliver;
import ent.entity.Group;
import ent.entity.Template;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import ent.entity.auth.SessionUser;
import ent.entity.product.Product;
import ent.enums.Role;
import ent.enums.State;
import ent.service.ExcelService;
import ent.service.Service;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CallBackHandler extends BaseMethods implements IBaseHandler {
    private final Session sessions;
    private final Service service;
    private final ExcelService excelService;

    public CallBackHandler(Bot bot, MarkupBoards markup, InlineBoards inline, Session sessions, Service service, ExcelService excelService, ExcelService excelService1) {
        super(bot, markup, inline);
        this.sessions = sessions;
        this.service = service;
        this.excelService = excelService1;
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        prepare(update);
        String data = update.getCallbackQuery().getData();
        if (!sessions.isBlocked(chatId)) {
            switch (data) {
                case "dochange" -> {
                    changeProduct(update);
                    return;
                }
                case "addT" -> {
                    addForSpecificDay(LocalDateTime.now());
                    return;
                }
                case "addTw" -> {
                    addForSpecificDay(LocalDateTime.now().plus(1, ChronoUnit.DAYS));
                    return;
                }
                case "editT" -> {
                    editSpecificDay(LocalDateTime.now(), update);
                    return;
                }
                case "editTw" -> {
                    editSpecificDay(LocalDateTime.now().plusDays(1), update);
                    return;
                }
                case "continue" -> {
                    editSpecificDay(sessions.getDay(chatId), update);
                    return;
                }
                case "ready" -> {
                    String changes = acceptChanges(sessions.getByChatId(chatId));
                    List<Group> groups = service.getAllGroups();
                    Template template = service.getTemplateByDay(sessions.getDay(chatId).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                    String path = excelService.editFile(template, sessions.getDay(chatId), service);
                    sendFile(groups, path);
                    sendMessageToGroups(groups, changes);
                    bot.executeMessage(eMsgObject(update, inline.storeKeeperPanel(), "<b>Taqsimot amalga oshirildi ✅</b>"));
                    return;
                }
                case "addDis" -> {
                    addUser(State.ADD_DIS);
                    return;
                }
                case "addAdmin" -> {
                    addUser(State.ADD_ADMIN);
                    return;
                }
            }
            if (data.startsWith("home")) {
                String role = data.split("#")[1];
                if (role.equals(Role.DISTRIBUTOR.getCode()))
                    bot.executeMessage(eMsgObject(update, inline.storeKeeperPanel(), "<b>Assalomu alaykum</b>"));
                else sendMessage(chatId, "<b>Assalomu alaykum</b>", markup.adminPanel());

            }
            if (data.startsWith("next")) {
                sessions.nextPage(chatId);
                controlPages(update, data);
            } else if (data.startsWith("previous")) {
                sessions.previousPage(chatId);
                controlPages(update, data);
            } else if (data.startsWith("product")) showProductInfo(data, update);
            else if (data.startsWith("group")) showGroupInfo(update, data);
            else if (data.startsWith("user")) {
                String userId = data.split("#")[1];
                AuthUser user = service.getById(Long.parseLong(userId));
                sendMessage(chatId, formatUserData(user), inline.userButton(user));
            } else if (data.startsWith("block")) {
                String id = data.split("#")[1];
                AuthUser byId = service.getById(Long.parseLong(id));
                if (!byId.getRegistered()) {
                    AnswerCallbackQuery query = new AnswerCallbackQuery();
                    query.setText("Ro'yxatdan o'tmagan foydalanuvchilarni bloklashning imkoni yo'q!");
                    query.setShowAlert(false);
                    query.setCallbackQueryId(callbackQuery.getId());
                    bot.executeMessage(query);
                    return;
                }
                if (byId.getChatId().equals(chatId)) {
                    AnswerCallbackQuery query = new AnswerCallbackQuery();
                    query.setText("Jarayonni amalga oshirib bo'lmadi!");
                    query.setShowAlert(false);
                    query.setCallbackQueryId(callbackQuery.getId());
                    bot.executeMessage(query);
                    return;
                }
                service.setUserBlockedStatus(id);
                byId.setBlocked(!byId.getBlocked());
                bot.executeMessage(eMsgObject(update, inline.userButton(byId), formatUserData(byId)));
                sessions.removeSession(byId.getChatId());
                if (byId.getChatId() != null)
                    sendMessage(byId.getChatId(), !byId.getBlocked() ? "Blokdan chiqarildingiz. Botdan foydaanish uchun /start bosing" : "Admin tomonidan bloklandingiz!", new ReplyKeyboardRemove(true));
            } else if (data.startsWith("remove")) {
                String id = data.split("#")[1];
                String role = data.split("#")[2];
                if (role.equals(Role.DISTRIBUTOR.getCode())) {
                    if (sessions.getByChatId(service.getById(Long.parseLong(id)).getChatId()) != null)
                        sessions.removeSession(service.getById(Long.parseLong(id)).getChatId());
                    service.removeDistributor(Long.parseLong(id));
                    bot.executeMessage(new DeleteMessage(chatId.toString(), message.getMessageId()));
                    return;
                }
                if (!sessions.getByChatId(chatId).getRole().equals(Role.OWNER)) {
                    AnswerCallbackQuery query = new AnswerCallbackQuery();
                    query.setText("Adminlarni o'shirish uchun huquqingiz yo'q!");
                    query.setShowAlert(false);
                    query.setCallbackQueryId(callbackQuery.getId());
                    bot.executeMessage(query);
                    return;
                }
                service.removeAdmin(Long.parseLong(id));
                if (sessions.getByChatId(service.getById(Long.parseLong(id)).getChatId()) != null)
                    sessions.removeSession(service.getById(Long.parseLong(id)).getChatId());
                sendMessage(chatId, "O'chirildi ✅", markup.adminPanel());
                bot.executeMessage(new DeleteMessage(chatId.toString(), message.getMessageId()));
            } else if (data.equals("close"))
                bot.executeMessage(new DeleteMessage(chatId.toString(), message.getMessageId()));
        } else sendMessage(chatId, "Bloklangansiz!", new ReplyKeyboardRemove(true));
    }

    private void sendFile(List<Group> groups, String path) {
        ExecutorService service = Executors.newFixedThreadPool(4);
        SendDocument document = new SendDocument();
        document.setDocument(new InputFile(new File(path)));
        Runnable runnable = (() -> {
            for (Group group : groups) {
                document.setChatId(group.getGroupId());
                bot.send(document);
            }
        });
        service.submit(runnable);
    }

    private void sendMessageToGroups(List<Group> groups, String changes) {
        ExecutorService service = Executors.newFixedThreadPool(4);
        Runnable runnable = (() -> {
            for (Group group : groups) {
                sendMessage(group.getGroupId(), changes);
            }
        });
        service.submit(runnable);
    }

    private String acceptChanges(SessionUser user) {
        int makroCount = 0;
        double makroPercent = 0D;
        StringBuilder changes = new StringBuilder("<b>" + user.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyy")) + " kun uchun TAQSIMOT bor:</b>\n");
        List<Product> products = service.getAllProductsByDay(user.getDate());
        for (Product product : products) {
            if (product.isEdited()) {
                product.setTotalCount(product.getNewCount());
                List<Deliver> delivers = product.getDelivers();
                for (Deliver deliver : delivers) {
                    if (deliver.getUsername().toUpperCase(Locale.ROOT).startsWith("MAKRO")) {
                        makroCount = deliver.getProductCount();
                        makroPercent = deliver.getPercent();
                        if (product.getTotalCount() < makroCount)
                            makroCount = (int) product.getTotalCount();
                    }
                    deliver.setPercent((deliver.getPercent()) + (makroPercent / delivers.size()));
                }
                changes.append("\n<b>- ").append(product.getName().toUpperCase()).append(", ").append("jami - ").append((int) product.getTotalCount()).append("ta, jumladan:</b>\n");
                setProductAsProportional(changes, product, delivers, makroCount);
            }
        }
        service.saveAllProduct(products);
        return changes.toString();
    }

    private void setProductAsProportional(StringBuilder changes, Product product, List<Deliver> delivers, int makroCount) {
        int totalCount = (int) product.getTotalCount() - makroCount;
        int totalInDelivers = 0;
        if (makroCount == product.getTotalCount()) {
            for (Deliver deliver : delivers) {
                if (deliver.getUsername().toUpperCase(Locale.ROOT).startsWith("MAKRO")) {
                    deliver.setProductCount(makroCount);
                } else deliver.setProductCount(0);
            }
            for (Deliver deliver : delivers) {
                if (deliver.isPresentInProduct())
                    changes.append("<i> ").append(deliver.getUsername()).append(" - ").append(deliver.getProductCount()).append("</i>\n");
            }
            return;
        }
        for (Deliver deliver : delivers) {
            if (deliver.isPresentInProduct()) {
                /* makro supermarket uchun alohida qism shart */
                if (!deliver.getUsername().toUpperCase(Locale.ROOT).startsWith("MAKRO")) {
                    deliver.setProductCount((int) Math.round((product.getNewCount() - makroCount) * (deliver.getPercent())));
                    totalInDelivers += deliver.getProductCount();
                }
            }
        }
        if (totalCount < totalInDelivers) {
            for (Deliver deliver : delivers) {
                while (totalCount != totalInDelivers)
                    if (deliver.isPresentInProduct()) {
                        if (!deliver.getUsername().toUpperCase(Locale.ROOT).startsWith("MAKRO")) {
                            deliver.setProductCount(deliver.getProductCount() - 1);
                            totalInDelivers--;
                        }
                    }
                break;
            }
        }
        if (totalCount > totalInDelivers) {
            while (totalCount != totalInDelivers)
                for (Deliver deliver : delivers) {
                    if (deliver.isPresentInProduct()) {
                        if (!deliver.getUsername().toUpperCase(Locale.ROOT).startsWith("MAKRO")) {
                            deliver.setProductCount(deliver.getProductCount() + 1);
                            totalInDelivers++;
                        }
                    }
                    if (totalCount == totalInDelivers) break;
                }
        }
        for (Deliver deliver : delivers) {
            if (deliver.isPresentInProduct())
                changes.append("<i> ").append(deliver.getUsername()).append(" - ").append(deliver.getProductCount()).append("</i>\n");
        }
    }

    private void showGroupInfo(Update update, String data) {
        String groupId = data.split("#")[1];
        service.modifyGroupAcceptable(groupId);
        bot.executeMessage(eMsgObject(update, inline.groupList(service.getGroups(sessions.getPage(chatId)), sessions.getPage(chatId))));
    }

    private void showProductInfo(String data, Update update) {
        String id = data.split("#")[1];
        sessions.setTempVal(id, chatId);
        sessions.setState(State.EDIT_PRODUCT, chatId);
        Product t = service.getProduct(id);
        bot.executeMessage(eMsgObject(update, "<b>Yangi miqdorni kiriting:\n\nMahsulot: <code>%s</code>\nSana: <code>%s</code>\nEski miqdor: <code>%s ta</code></b>"
                .formatted(t.getName(),
                        t.getDay(),
                        (int) t.getCount())));
    }

    private void changeProduct(Update update) {
        switch (sessions.getRole(chatId)) {
            case DISTRIBUTOR -> bot.executeMessage(eMsgObject(update, inline.dayButtons(sessions.getRole(chatId)), "<b>Sanani tanlang</b>"));
            default -> {
            }
        }
    }

    private void addUser(State state) {
        sessions.setState(state, chatId);
        bot.executeMessage(new DeleteMessage(chatId.toString(), message.getMessageId()));
        sendMessage(chatId, "Foydalanuvchi nomini kiriting:\n<i>Misol uchun: @akbarovich</i>", markup.cancel());
    }

    private void editSpecificDay(LocalDateTime day, Update update) {
        sessions.setState(State.DEFAULT, chatId);
        sessions.setDate(day, chatId);
        showProducts(update, day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    private void addForSpecificDay(LocalDateTime day) {
        sessions.setState(State.UPLOAD_FORM, chatId);
        sessions.setDate(day, chatId);
        sendMessage(chatId, "<b>Jadvalni yuklang</b>", markup.cancel());
        bot.executeMessage(new DeleteMessage(chatId.toString(), message.getMessageId()));
    }

    private void showProducts(Update update, String day) {
        sessions.setPageZero(chatId);
        List<Product> productList = service.getAllProducts(sessions.getDay(chatId), sessions.getPage(chatId));
        if (productList.isEmpty()) {
            bot.executeMessage(eMsgObject(update, inline.dayButtons(Role.DISTRIBUTOR), "<i>%s sana uchun mahsulotlar ro'yxati kiritilmagan</i>".formatted(day)));
            return;
        }
        bot.executeMessage(eMsgObject(update, inline.productList(productList, sessions.getPage(chatId)), "<b>Mahsulotlar</b>"));
    }

    private void controlPages(Update update, String data) {
        if (data.endsWith("dis"))
            bot.executeMessage(eMsgObject(update, inline.userList(service.getAllDistributors(sessions.getPage(chatId)), sessions.getPage(chatId), Role.DISTRIBUTOR)));
        else if (data.endsWith("gr"))
            bot.executeMessage(eMsgObject(update, inline.groupList(service.getGroups(sessions.getPage(chatId)), sessions.getPage(chatId))));
        else if (data.endsWith("product")) {
            bot.executeMessage(eMsgObject(update, inline.productList(service.getAllProducts(sessions.getDay(chatId), sessions.getPage(chatId)), sessions.getPage(chatId))));
        } else if (data.endsWith("admin"))
            bot.executeMessage(eMsgObject(update, inline.userList(service.getAllAdmins(sessions.getPage(chatId)), sessions.getPage(chatId), Role.ADMIN)));
    }

    private String formatUserData(AuthUser user) {
        String status = user.getBlocked() ? "bloklangan" : "faol";
        String s;
        if (user.getRegistered()) {
            s = "<b>👮🏻 Username: </b><code>" + user.getUsername() + "</code>\n" +
                    "<b>🛡 Maqom: </b><code>" + user.getRole() + "</code>\n" +
                    "<b>ℹ️ Holat: </b><code>" + status + "</code>";
        } else {
            s = "<b>👮🏻 Username: </b><code>" + user.getUsername() + "</code>\n" +
                    "<b>🛡 Maqom: </b><code>" + user.getRole() + "</code>\n" +
                    "<b>ℹ️ Holat: </b><code>" + "unregistered" + "</code>";
        }
        return s;
    }
}
