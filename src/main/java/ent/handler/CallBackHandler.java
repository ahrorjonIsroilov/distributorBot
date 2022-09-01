package ent.handler;

import ent.Bot;
import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import ent.enums.Role;
import ent.enums.State;
import ent.service.Service;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

@Component
public class CallBackHandler extends BaseMethods implements IBaseHandler {
    private final Session sessions;
    private final Service service;

    public CallBackHandler(Bot bot, MarkupBoards markup, InlineBoards inline, Session sessions, Service service) {
        super(bot, markup, inline);
        this.sessions = sessions;
        this.service = service;
    }


    @Override
    public void handle(Update update) {
        prepare(update);
        String data = update.getCallbackQuery().getData();
        if (data.equals("addDis")) {
            sessions.setState(State.ADD_DIS, chatId);
            bot.executeMessage(new DeleteMessage(chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
            sendMessage(chatId, "Foydalanuvchi nomini kiriting:\n<i>Misol uchun: @akbarovich</i>", markup.cancel());
        }
        if (data.equals("addAdmin")) {
            sessions.setState(State.ADD_ADMIN, chatId);
            bot.executeMessage(new DeleteMessage(chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
            sendMessage(chatId, "Foydalanuvchi nomini kiriting:\n<i>Misol uchun: @akbarovich</i>", markup.cancel());
        } else if (data.startsWith("next")) {
            sessions.nextPage(chatId);
            if (data.endsWith("dis"))
                bot.executeMessage(eMsgObject(update, inline.userList(service.getAllDistributors(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage(), Role.DISTRIBUTOR)));
            else if (data.endsWith("gr"))
                bot.executeMessage(eMsgObject(update, inline.groupList(service.getGroups(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage())));
            else if (data.endsWith("product")) {
            } else if (data.endsWith("admin"))
                bot.executeMessage(eMsgObject(update, inline.userList(service.getAllAdmins(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage(), Role.ADMIN)));
        } else if (data.startsWith("previous")) {
            sessions.previousPage(chatId);
            if (data.endsWith("dis"))
                bot.executeMessage(eMsgObject(update, inline.userList(service.getAllDistributors(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage(), Role.DISTRIBUTOR)));
            else if (data.endsWith("gr"))
                bot.executeMessage(eMsgObject(update, inline.groupList(service.getGroups(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage())));
            else if (data.endsWith("product")) {
            } else if (data.endsWith("admin"))
                bot.executeMessage(eMsgObject(update, inline.userList(service.getAllAdmins(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage(), Role.ADMIN)));
        } else if (data.startsWith("group")) {
            String groupId = data.split("#")[1];
            service.modifyGroupAcceptable(groupId);
            bot.executeMessage(eMsgObject(update, inline.groupList(service.getGroups(sessions.getByChatId(chatId).getPage()), sessions.getByChatId(chatId).getPage())));
        } else if (data.startsWith("user")) {
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
            if (byId.getChatId() != null)
                sendMessage(byId.getChatId(), byId.getBlocked() ? "Blokdan chiqarildingiz. Botdan foydaanish uchun /start bosing" : "Admin tomonidan bloklandingiz!", new ReplyKeyboardRemove(true));
            service.setUserBlockedStatus(id);
            byId.setBlocked(!byId.getBlocked());
            sessions.removeSession(byId.getChatId());
            bot.executeMessage(eMsgObject(update, inline.userButton(byId), formatUserData(byId)));
        } else if (data.startsWith("remove")) {
            String id = data.split("#")[1];
            String role = data.split("#")[2];
            if (role.equals(Role.DISTRIBUTOR.getCode()))
                service.removeDistributor(Long.parseLong(id));
            else {
                if (!sessions.getByChatId(chatId).getRole().equals(Role.OWNER)) {
                    AnswerCallbackQuery query = new AnswerCallbackQuery();
                    query.setText("Adminlarni o'shirish uchun huquqingiz yo'q!");
                    query.setShowAlert(false);
                    query.setCallbackQueryId(callbackQuery.getId());
                    bot.executeMessage(query);
                    return;
                }
                service.removeAdmin(Long.parseLong(id));
            }
            sendMessage(chatId, "O'chirildi ✅", markup.adminPanel());
            bot.executeMessage(new DeleteMessage(chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
        } else if (data.equals("close")) {
            bot.executeMessage(new DeleteMessage(chatId.toString(), update.getCallbackQuery().getMessage().getMessageId()));
        }
    }

    private String formatUserData(AuthUser user) {
        String status = user.getBlocked() ? "bloklangan" : "faol";
        String s;
        if (user.getRegistered()) {
            s = "<b>👮🏻 Username: </b><code>" + user.getUsername() + "</code>\n\n" +
                    "<b>🛡 Maqom: </b><code>" + user.getRole() + "</code>\n\n" +
                    "<b>ℹ️Holat: </b><code>" + status + "</code>";
        } else {
            s = "<b>👮🏻 Username: </b><code>" + user.getUsername() + "</code>\n" +
                    "<b>🛡 Maqom: </b><code>" + user.getRole() + "</code>\n" +
                    "<b>ℹ️ Holat: </b><code>" + "unregistered" + "</code>";
        }
        return s;
    }
}
