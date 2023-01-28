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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CallBackHandler extends BaseMethods implements IBaseHandler {
    private final Session sessions;
    private final Service service;
    private final ExcelService excelService;

    public CallBackHandler(Bot bot, MarkupBoards markup, InlineBoards inline, Session sessions, Service service, ExcelService excelService) {
        super(bot, markup, inline);
        this.sessions = sessions;
        this.service = service;
        this.excelService = excelService;
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
                    addForSpecificDay(LocalDateTime.now().plusDays(1));
                    return;
                }
                case "editT" -> {
                    showExclusions(LocalDateTime.now(), update);
                    return;
                }
                case "editTw" -> {
                    showExclusions(LocalDateTime.now().plusDays(1), update);
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
                case "continue-exclusions" -> {
                    showExclusionProducts(sessions.getExclusionName(chatId), update);
                }
                case "back-to-exclusion" -> {
                    showExclusions(sessions.getDay(chatId), update);
                }
                case "ready-to-others" -> {
                    editSpecificDay(sessions.getDay(chatId), update);
                }
            }
            if (data.startsWith("home")) {
                String role = data.split("#")[1];
                if (role.equals(Role.DISTRIBUTOR.getCode()))
                    bot.executeMessage(eMsgObject(update, inline.storeKeeperPanel(), "<b>Assalomu alaykum</b>"));
                else sendMessage(chatId, "<b>Assalomu alaykum</b>", markup.adminPanel());

            }
            if (data.startsWith("exclusion")) {
                String exclusionName = data.split("#")[1];
                sessions.setExclusionName(exclusionName, chatId);
                showExclusionProducts(exclusionName, update);
            }
            if (data.startsWith("next")) {
                sessions.nextPage(chatId);
                controlPages(update, data, sessions.getExclusionName(chatId));
            } else if (data.startsWith("previous")) {
                sessions.previousPage(chatId);
                controlPages(update, data, sessions.getExclusionName(chatId));
            } else if (data.startsWith("product")) showProductInfo(data, update);
            else if (data.startsWith("prod-exclusion")) showProductInfoForExclusion(data, update);
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
        } else
            sendMessage(chatId, "Bloklangansiz!", new ReplyKeyboardRemove(true));
    }

    private void showExclusionProducts(String exclusionName, Update update) {
        sessions.setPageZero(chatId);
        List<Product> products = service.getAllProducts(sessions.getDay(chatId), sessions.getPage(chatId));
        bot.executeMessage(eMsgObject(update, inline.productListForExclusion(exclusionName, products, sessions.getPage(chatId)), "<b>%s</b> do'kon uchun o'zgarishlar".formatted(exclusionName)));
    }

    private void showExclusions(LocalDateTime now, Update update) {
        sessions.setPageZero(chatId);
        List<Product> productList = service.getAllProducts(now, sessions.getPage(chatId));
        if (productList.isEmpty()) {
            bot.executeMessage(popupMessage("%s sana uchun mahsulotlar ro'yxati kiritilmagan".formatted(now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))), callbackQuery.getId()));
            return;
        }
        sessions.setState(State.DEFAULT, chatId);
        sessions.setDate(now, chatId);
        bot.executeMessage(eMsgObject(update, inline.showExclusions(), "Quyidagi do'konlar uchun o'zgarish bo'lmasa davom etish tugmasini bosing"));
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
        StringBuilder changes = new StringBuilder("<b>" + user.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyy")) + " kun uchun TAQSIMOT bor:</b>\n");
        List<Product> products = service.getAllProductsByDay(user.getDate());
        for (Product product : products) {
            if (product.isEdited()) {
                product.setTotalCount(product.getCount());
                product.setNewCount(product.getCount());
                List<Deliver> delivers = product.deliversWithoutExclusions();
                List<Deliver> productDelivers = product.getDelivers();
                List<Deliver> exclusionDelivers = product.exclusionDelivers();
                changes.append("\n<b>- ").append(product.getName().toUpperCase()).append(", ").append("jami - ").append((int) product.getTotalCount()).append("ta, jumladan:</b>\n");
                setProductAsProportional(changes, product, productDelivers, delivers, exclusionDelivers);
            }
        }
        service.saveAllProduct(products);
        return changes.toString();
    }

    private void setProductAsProportional(StringBuilder changes, Product product, List<Deliver> productDelivers, List<Deliver> deliversWithoutExclusions, List<Deliver> exclusionDelivers) {
        int totalCount = (int) product.getTotalCount();
        for (Deliver deliver : exclusionDelivers) totalCount -= deliver.getProductCount();
        int totalInDelivers = 0;
        for (Deliver deliver : deliversWithoutExclusions) {
            if (deliver.isPresentInProduct()) {
                deliver.setProductCount((int) Math.round(product.getNewCount() * deliver.getPercent()));
                totalInDelivers += deliver.getProductCount();
            }
        }
        if (totalCount < totalInDelivers) {
            while (totalCount != totalInDelivers) for (Deliver deliver : deliversWithoutExclusions) {
                if (deliver.isPresentInProduct()) {
                    deliver.setProductCount(deliver.getProductCount() - 1);
                    totalInDelivers--;
                    if (totalCount == totalInDelivers) break;
                }
            }
        }
        if (totalCount > totalInDelivers) {
            while (totalCount != totalInDelivers) for (Deliver deliver : deliversWithoutExclusions) {
                if (deliver.isPresentInProduct()) {
                    deliver.setProductCount(deliver.getProductCount() + 1);
                    totalInDelivers++;
                    if (totalCount == totalInDelivers) break;
                }
            }
        }
        for (Deliver deliver : productDelivers) {
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

    private void showProductInfoForExclusion(String data, Update update) {
        String id = data.split("#")[1];
        sessions.setTempVal(id, chatId);
        sessions.setState(State.EDIT_PRODUCT_FOR_EXCLUSION, chatId);
        Product product = service.getProduct(id);
        Deliver d = product.exclusionDelivers().stream().filter(deliver -> deliver.getUsername().equalsIgnoreCase(sessions.getExclusionName(chatId))).findFirst().orElse(null);
        bot.executeMessage(eMsgObject(update, "<b>%s do'kon uchun yangi miqdorni kiriting:\n\nMahsulot: <code>%s</code>\nSana: <code>%s</code>\nEski miqdor: <code>%s ta</code></b>"
            .formatted(sessions.getExclusionName(chatId), product.getName(),
                product.getDay(),
                d.getProductCount())));
    }

    private void changeProduct(Update update) {
        if (Objects.requireNonNull(sessions.getRole(chatId)) == Role.DISTRIBUTOR) {
            bot.executeMessage(eMsgObject(update, inline.dayButtons(sessions.getRole(chatId)), "<b>Sanani tanlang</b>"));
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
            bot.executeMessage(popupMessage("<i>%s sana uchun mahsulotlar ro'yxati kiritilmagan</i>".formatted(day), callbackQuery.getId()));
            return;
        }
        bot.executeMessage(eMsgObject(update, inline.productList(productList, sessions.getPage(chatId)), "<b>Mahsulotlar</b>"));
    }

    private void controlPages(Update update, String data, String exclusionName) {
        if (data.endsWith("dis"))
            bot.executeMessage(eMsgObject(update, inline.userList(service.getAllDistributors(sessions.getPage(chatId)), sessions.getPage(chatId), Role.DISTRIBUTOR)));
        else if (data.endsWith("gr"))
            bot.executeMessage(eMsgObject(update, inline.groupList(service.getGroups(sessions.getPage(chatId)), sessions.getPage(chatId))));
        else if (data.endsWith("product")) {
            bot.executeMessage(eMsgObject(update, inline.productList(service.getAllProducts(sessions.getDay(chatId), sessions.getPage(chatId)), sessions.getPage(chatId))));
        } else if (data.endsWith("prod-exclusion")) {
            bot.executeMessage(eMsgObject(update, inline.productListForExclusion(sessions.getExclusionName(chatId), service.getAllProducts(sessions.getDay(chatId), sessions.getPage(chatId)), sessions.getPage(chatId))));
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
