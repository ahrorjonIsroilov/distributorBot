package ent.service;

import ent.entity.Group;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import ent.enums.Role;
import ent.repo.GroupRepo;
import ent.repo.auth.AuthRepo;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.objects.Contact;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class Service implements BaseService {
    private final AuthRepo repo;
    private final GroupRepo groupRepo;
    private final Session sessions;

    @Lazy
    public Service(AuthRepo repo, GroupRepo groupRepo, Session sessions) {
        this.repo = repo;
        this.groupRepo = groupRepo;
        this.sessions = sessions;
    }

    public void save(AuthUser user) {
        repo.save(user);
    }

    public AuthUser getByChatId(Long chatId) {
        return repo.findByChatId(chatId).orElse(null);
    }

    public List<AuthUser> getAll() {
        return (List<AuthUser>) repo.findAll();
    }

    public Boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    public Boolean existsByChatId(Long chatId) {
        return repo.existsByChatIdAndRegisteredTrue(chatId);
    }

    public AuthUser getByUsername(String username) {
        return repo.findByUsername(username);
    }

    public void deleteByChatId(Long chatId) {
        repo.deleteByChatId(chatId);
    }

    public void setRegistered(Long chatId, Boolean status) {
        repo.registerUser(chatId, status);
    }

    public Boolean isRegistered(Long chatId) {
        if (!sessions.existsByChatId(chatId)) {
            if (Objects.nonNull(repo.findByChatIdAndRegisteredTrue(chatId)))
                sessions.setSession(chatId);
        }
        return sessions.existsByChatId(chatId);
    }

    public Boolean blocked(Long chatId) {
        if (!sessions.existsByChatId(chatId)) {
            return repo.findByChatIdAndRegisteredTrue(chatId).getBlocked();
        }
        return false;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void clearLog() {
        try {
            FileUtils.forceDelete(new File("logs/archived"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String preparePhoneNumber(Contact contact) {
        String phoneNumber = contact.getPhoneNumber();
        if (phoneNumber.startsWith("+")) phoneNumber = phoneNumber.substring(1);
        return phoneNumber;
    }

    public void register(Long chatId, String username, String fullname) {
        AuthUser user = repo.findByUsername(username);
        user.setChatId(chatId);
        user.setRegistered(true);
        user.setFullName(fullname);
        save(user);
    }

    public void register(AuthUser user) {
        repo.save(user);
    }

    public List<Group> getGroups(Integer page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt"));
        return groupRepo.findAllBy(pageable);
    }

    public List<AuthUser> getAllDistributors(Integer page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt"));
        return repo.findAllByRole(Role.DISTRIBUTOR, pageable);
    }

    public List<AuthUser> getAllAdmins(Integer page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt"));
        return repo.findAllByRole(Role.ADMIN, pageable);
    }

    public List<AuthUser> getAllProducts(Integer page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt"));
        return repo.findAllByRole(Role.ADMIN, pageable);
    }

    public Group findGroupByGroupId(Long groupId) {
        return groupRepo.findByGroupId(groupId);
    }

    public void modifyGroupAcceptable(String groupId) {
        Group group = findGroupByGroupId(Long.parseLong(groupId));
        group.setAccepted(!group.getAccepted());
        groupRepo.save(group);
    }

    public AuthUser getById(long id) {
        return repo.getById(id);
    }

    public void setUserBlockedStatus(String id) {
        AuthUser user = getById(Long.parseLong(id));
        repo.setUserBlockedStatus(user.getId(), !user.getBlocked());
    }

    public void removeDistributor(long id) {
        repo.deleteById(id);
    }

    public void removeAdmin(long id) {
        repo.deleteById(id);
    }
}
