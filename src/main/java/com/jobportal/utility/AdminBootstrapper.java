package com.jobportal.utility;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.jobportal.dto.AccountType;
import com.jobportal.dto.UserDTO;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.UserService;

@Component
public class AdminBootstrapper implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${app.admin.default-email:admin@jobhook.com}")
    private String defaultAdminEmail;

    @Value("${app.admin.default-password:Admin@123}")
    private String defaultAdminPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<User> admins = userRepository.findByAccountType(AccountType.ADMIN);
        
        if (admins.isEmpty()) {
            System.out.println("[AdminBootstrapper] No ADMIN account found. Creating default admin...");
            
            try {
                UserDTO adminUser = new UserDTO();
                adminUser.setName("System Admin");
                adminUser.setEmail(defaultAdminEmail);
                adminUser.setPassword(defaultAdminPassword);
                adminUser.setAccountType(AccountType.ADMIN);
                
                userService.registerUser(adminUser);
                System.out.println("[AdminBootstrapper] Default admin account created successfully.");
                System.out.println("Email: " + defaultAdminEmail);
            } catch (Exception e) {
                System.err.println("[AdminBootstrapper] Failed to create default admin: " + e.getMessage());
            }
        } else {
            System.out.println("[AdminBootstrapper] Admin account already exists. Skipping bootstrap.");
        }
    }
}
