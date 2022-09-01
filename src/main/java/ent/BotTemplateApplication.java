package ent;

import ent.config.BotRunner;
import ent.entity.auth.AuthUser;
import ent.enums.State;
import ent.service.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BotTemplateApplication implements CommandLineRunner {

    private final Service service;
    private final BotRunner runner;

    public static void main(String[] args) {
        SpringApplication.run(BotTemplateApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        runner.main();
//        migrate();
    }

    private void migrate() {
        AuthUser authUser = AuthUser.builder()
                .chatId(1055174667L)
                .username("akhdeo")
                .fullName("Ahrorjon")
                .phoneNumber("998903061599")
                .registered(true)
                .page(0)
                .state(State.DEFAULT)
                .build();
        service.save(authUser);
    }
}
