package ent.handler;


import ent.Bot;
import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import ent.entity.Group;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import ent.enums.Role;
import ent.enums.State;
import ent.service.Service;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.List;
import java.util.Objects;

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
        if (service.isRegistered(chatId)) {
            if (!service.blocked(chatId)) {

                if (sessions.checkRole(chatId, Role.ADMIN, Role.OWNER)) {
                    if (messageTextEquals(update, "/start")) {
                        sendMessage(chatId, "<b>Assalomu alaykum</b>", markup.adminPanel());
                    } else if (messageTextEquals(update, "Bekor qilish ❌")) {
                        sessions.setState(State.DEFAULT, chatId);
                        sendMessage(chatId, "Bekor qilindi", markup.adminPanel());
                    } else if (messageTextEquals(update, "Guruhlar 👥", State.DEFAULT)) {
                        sessions.setPageZero(chatId);
                        List<Group> groups = service.getGroups(sessions.getByChatId(chatId).getPage());
                        if (groups.size() < 1) {
                            sendMessage(chatId, "Guruhlar topilmadi. Botni guruhga qo'shish uchun pastdagi tugmani bosing 👇", inline.addToGroup());
                            return;
                        }
                        sendMessage(chatId, "Guruhlar ro'yxati\n[Tanlangan guruhlar qatoriga qo'shish yoki o'chirish uchun guruh ustiga bosing]", inline.groupList(groups, sessions.getByChatId(chatId).getPage()));
                    } else if (messageTextEquals(update, "Taqsimotchilar 🧢", State.DEFAULT)) {
                        sessions.setPageZero(chatId);
                        List<AuthUser> distributors = service.getAllDistributors(sessions.getByChatId(chatId).getPage());
                        if (distributors.size() < 1) {
                            sendMessage(chatId, "Taqsimotchilar mavjud emas", inline.addDistributor());
                            return;
                        }
                        sendMessage(chatId, "Taqsimotchilar", inline.userList(distributors, sessions.getByChatId(chatId).getPage(), Role.DISTRIBUTOR));
                    } else if (messageTextEquals(update, "/admins")) {
                        sessions.setPageZero(chatId);
                        List<AuthUser> admins = service.getAllAdmins(sessions.getByChatId(chatId).getPage());
                        if (admins.size() < 1) {
                            sendMessage(chatId, "Adminlar mavjud emas", inline.addDistributor());
                            return;
                        }
                        sendMessage(chatId, "Adminlar", inline.userList(admins, sessions.getByChatId(chatId).getPage(), Role.ADMIN));
                    } else if (sessions.checkState(State.ADD_DIS, chatId)) {
                        addUser(update, Role.DISTRIBUTOR, "Taqsimotchi");
                    } else if (sessions.checkState(State.ADD_ADMIN, chatId)) {
                        addUser(update, Role.ADMIN, "Admin");
                    }
                } else if (sessions.checkRole(chatId, Role.DISTRIBUTOR)) {
                    
                }
            } else {
                sendMessage(chatId, "Bloklangansiz!", new ReplyKeyboardRemove(true));
            }
        } else if (!hasUsername(update)) {
            sendMessage(chatId, "<b>Kechirasiz! Botni faqat telegram foydalanuvchi nomiga ega insonlar ishlatishlari mumkin 😕</b>");
        } else if (service.existsByUsername(message.getFrom().getUserName()) && !service.getByUsername(message.getFrom().getUserName()).getRole().equals(Role.ADMIN)) {
            service.register(chatId, update.getMessage().getFrom().getUserName(), update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName());
            sessions.setSession(chatId);
            sendMessage(chatId, "<b>Assalomu alaykum</b>", markup.storekeeperPanel());
        } else if (service.existsByUsername(message.getFrom().getUserName()) && service.getByUsername(message.getFrom().getUserName()).getRole().equals(Role.ADMIN)) {
            service.register(chatId, update.getMessage().getFrom().getUserName(), update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName());
            sessions.setSession(chatId);
            sendMessage(chatId, "<b>Assalomu alaykum</b>", markup.adminPanel());
        } else {
            if (messageTextEquals(update, "/start")) {
                service.register(AuthUser.builder()
                        .chatId(update.getMessage().getFrom().getId())
                        .username(update.getMessage().getFrom().getUserName())
                        .fullName(update.getMessage().getFrom().getFirstName() + update.getMessage().getFrom().getLastName())
                        .page(0)
                        .state(State.DEFAULT)
                        .registered(false)
                        .blocked(false)
                        .phoneNumber("NONE")
                        .build());
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

    private boolean messageTextEquals(Update update, String text, State state) {
        return update.getMessage().hasText() && update.getMessage().getText().equals(text) && sessions.checkState(state, chatId);
    }

    private boolean messageHasText(Update update) {
        return update.getMessage().hasText();
    }

    private boolean messageTextEquals(Update update, String text) {
        return update.getMessage().hasText() && update.getMessage().getText().equals(text);
    }

    private boolean hasUsername(Update update) {
        return Objects.nonNull(update.getMessage().getFrom().getUserName());
    }
}
