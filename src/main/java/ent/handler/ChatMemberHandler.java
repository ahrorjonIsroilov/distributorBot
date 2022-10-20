package ent.handler;

import ent.Bot;
import ent.button.InlineBoards;
import ent.button.MarkupBoards;
import ent.entity.Group;
import ent.repo.GroupRepo;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ChatMemberHandler extends BaseMethods implements IBaseHandler {
    private final GroupRepo groupRepo;

    public ChatMemberHandler(Bot bot, MarkupBoards markup, InlineBoards inline, GroupRepo groupRepo) {
        super(bot, markup, inline);
        this.groupRepo = groupRepo;
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        if (join(update)) {
            if (!groupRepo.existsByGroupId(update.getMyChatMember().getChat().getId())) {
                Group group = Group.builder()
                        .groupId(update.getMyChatMember().getChat().getId())
                        .title(update.getMyChatMember().getChat().getTitle())
                        .accepted(false)
                        .build();
                groupRepo.save(group);
            }
            sendMessage(update.getMyChatMember().getFrom().getId(), "<b>Assalomu alaykum</b>\n<i>Siz botni <b>\"%s\"</b> guruhiga qo'shdingiz</i>".formatted(update.getMyChatMember().getChat().getTitle()));
        } else if (left(update)) {
            groupRepo.deleteByGroupId(update.getMyChatMember().getChat().getId());
        }
    }

    private boolean join(Update update) {
        return (update.getMyChatMember().getNewChatMember().getStatus().equals("member") && update.getMyChatMember().getNewChatMember().getUser().getUserName().equals(bot.getBotUsername().substring(1)));
    }

    private boolean left(Update update) {
        return (update.getMyChatMember().getNewChatMember().getStatus().equals("left") && update.getMyChatMember().getNewChatMember().getUser().getUserName().equals(bot.getBotUsername().substring(1)));
    }
}
