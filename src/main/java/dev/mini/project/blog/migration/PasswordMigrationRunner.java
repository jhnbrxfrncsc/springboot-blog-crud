package dev.mini.project.blog.migration;

import dev.mini.project.blog.model.entity.User;
import dev.mini.project.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("migration")
@RequiredArgsConstructor
@Slf4j
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("PasswordMigrationRunner#run() -- START");
        List<User> users =  userRepository.findAll();

        for(User user: users) {
            if(!user.isPasswordMigrated()){
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setPasswordMigrated(true);

                log.info("User({})'s password has already been hashed!", user.getUsername());
                userRepository.save(user);
            }
        }

        log.info("✅ Password migration completed");
        log.info("PasswordMigrationRunner#run() -- END");
    }
}
