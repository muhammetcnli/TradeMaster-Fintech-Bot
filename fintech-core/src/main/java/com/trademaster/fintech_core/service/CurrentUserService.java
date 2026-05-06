package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID requireCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
        }

        return principal.userId();
    }
}

