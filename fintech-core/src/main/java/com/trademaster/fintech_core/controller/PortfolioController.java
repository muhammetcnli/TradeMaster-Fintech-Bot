package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.entity.UserAsset;
import com.trademaster.fintech_core.repository.UserRepository;
import com.trademaster.fintech_core.service.UserService;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@NoArgsConstructor
@RequestMapping("api/v1/portfolio")
public class PortfolioController {

    private UserService userService;

    @GetMapping
    public ResponseEntity<PortfolioDTO> getUserPortfolio(@PathVariable UUID id){
            User user = userService.getUserById(id);

            List<UserAsset> assets = user.getAssets();

    }

}
