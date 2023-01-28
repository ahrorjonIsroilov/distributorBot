package ent.service;

import ent.Bot;
import ent.button.MarkupBoards;
import ent.entity.Deliver;
import ent.entity.Group;
import ent.entity.Template;
import ent.entity.auth.AuthUser;
import ent.entity.auth.Session;
import ent.entity.product.Product;
import ent.enums.Role;
import ent.repo.DeliverRepo;
import ent.repo.GroupRepo;
import ent.repo.ProductRepo;
import ent.repo.TemplateRepo;
import ent.repo.auth.AuthRepo;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EnableScheduling
@org.springframework.stereotype.Service
public class Service implements BaseService {
    private final AuthRepo repo;
    private final ProductRepo productRepo;
    private final GroupRepo groupRepo;
    private final TemplateRepo templateRepo;
    private final DeliverRepo deliverRepo;
    private final Session sessions;
    private final ExcelService excelService;
    private final Bot bot;

    @Lazy
    public Service(AuthRepo repo, ProductRepo productRepo, GroupRepo groupRepo, TemplateRepo templateRepo, DeliverRepo deliverRepo, Session sessions, ExcelService excelService, Bot bot) {
        this.repo = repo;
        this.productRepo = productRepo;
        this.groupRepo = groupRepo;
        this.templateRepo = templateRepo;
        this.deliverRepo = deliverRepo;
        this.sessions = sessions;
        this.excelService = excelService;
        this.bot = bot;
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

    public Boolean existsByUsernameAndRegisteredFalse(String username) {
        return repo.existsByUsernameAndRegisteredFalse(username);
    }

    public Boolean existsByUsernameAndRegisteredTrue(String username) {
        return repo.existsByUsernameAndRegisteredFalse(username);
    }

    public Boolean existsByChatId(Long chatId) {
        return repo.existsByChatIdAndRegisteredTrue(chatId);
    }

    public Boolean existsByChatIdRegisteredFalse(Long chatId) {
        return repo.existsByChatIdAndRegisteredFalse(chatId);
    }

    public AuthUser getByUsername(String username) {
        return repo.findByUsername(username);
    }

    public Boolean isRegistered(Long chatId) {
        if (!sessions.existsByChatId(chatId)) {
            if (Objects.nonNull(repo.findByChatIdAndRegisteredTrue(chatId)))
                sessions.setSession(chatId);
        }
        return sessions.existsByChatId(chatId);
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

    public List<Product> getAllProductsByDay(LocalDateTime day) {
        return productRepo.getAllByDayAndTotalCountGreaterThan(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), -1D);
    }

    public List<Product> getAllProducts(LocalDateTime day, int page) {
        return productRepo.getAllByDay(day.format(DateTimeFormatter.ofPattern("dd-MM-yyy")), PageRequest.of(page, 10, Sort.by("count").descending()));
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

    @Scheduled(cron = "0 0 12 * * ?")
    public void sendNotification() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        Runnable runnable = (() -> {
            List<AuthUser> admins = repo.findAllByRole(Role.ADMIN);
            for (AuthUser admin : admins) {
                SendMessage message = new SendMessage();
                message.setChatId(admin.getChatId());
                message.setText("<b>Ertangi kun uchun jadvalni yuklash esingizdan chiqmasin</b>");
                message.enableHtml(true);
                bot.executeMessage(message);
            }
            SendMessage message = new SendMessage();
            message.setChatId(1120321L);
            message.setText("<b>Ertangi kun uchun jadvalni yuklash esingizdan chiqmasin</b>");
            message.enableHtml(true);
            bot.executeMessage(message);
        });
        executorService.submit(runnable);
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void clearTemplate() {
        templateRepo.deleteByDate(LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

    }

    @Scheduled(cron = "0 0 6 * * ?")
    public void sendAllCorrectNotification() {
        ExecutorService service = Executors.newFixedThreadPool(4);
        boolean edited = templateRepo.existsByDateAndEditedTrue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        Runnable runnable = (() -> {
            if (!edited) {
                List<Group> groups = groupRepo.getAllByAccepted(true);
                for (Group group : groups) {
                    SendMessage message = new SendMessage(group.getGroupId().toString(), "<i>Bugun taqsimot yo'q. Barcha mahsulotlar yetarli. Kuningiz xayrli bo'lsin, savdoingizga baraka bersin!</i>");
                    message.enableHtml(true);
                    bot.executeMessage(message);
                }
            }
        });
        service.submit(runnable);
    }

    public void removeDistributor(long id) {
        repo.deleteById(id);
    }

    public void removeAdmin(long id) {
        repo.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    public void readData(String path, LocalDateTime day, Update update, Bot bot, MarkupBoards markupBoards) {
        excelService.readData(path, day, update, bot, markupBoards);
    }

    public boolean editProduct(String id, Integer count) {
        Product byId = productRepo.getById(Long.parseLong(id));
        if (byId.getTotalCount() < count) return false;
        byId.setCount(count);
        byId.setNewCount(count);
        byId.setTotalCount(count);
        byId.setEdited(true);
        productRepo.save(byId);
        return true;
    }

    public boolean editProductForExclusion(String id, Integer count, String exclusionName) {
        Product byId = productRepo.getById(Long.parseLong(id));
        if (byId.getTotalCount() < count) return false;
        List<Deliver> delivers = byId.getDelivers();
        for (Deliver deliver : delivers) {
            if (deliver.getUsername().equals(exclusionName)) {
                if (deliver.isPresentInProduct()) {
                    deliver.setProductCount(count);
                    byId.setEdited(true);
                }
            }
        }
        productRepo.save(byId);
        return true;
    }

    public void saveAllProduct(List<Product> products) {
        productRepo.saveAll(products);
    }

    public List<Group> getAllGroups() {
        return groupRepo.getAllByAccepted(true);
    }

    public Product getProduct(String id) {
        return productRepo.getById(Long.parseLong(id));
    }

    public Template getTemplateByDay(String day) {
        return templateRepo.findFirstByDate(day);
    }

    public List<Product> getAllProductsByDeliver(String exclusionName, LocalDateTime day, Integer page) {
        return productRepo.getAllByDayAndDeliversIn(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), List.of(Deliver.builder().username(exclusionName).build()), PageRequest.of(page, 10));
    }
}
