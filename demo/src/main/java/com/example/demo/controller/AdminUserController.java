package com.example.demo.controller;

import com.example.demo.entity.AppUser;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.UserSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AppUserRepository appUserRepository;
    private final UserSessionRepository userSessionRepository;

    public AdminUserController(AppUserRepository appUserRepository, UserSessionRepository userSessionRepository) {
        this.appUserRepository = appUserRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deleteByUsername(@PathVariable String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
        userSessionRepository.deleteByUserId(user.getId());
        appUserRepository.delete(user);
    }
}
