package com.monew.domain.activity.controller;

import com.monew.domain.activity.document.UserActivity;
import com.monew.domain.activity.service.UserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-activities")
public class UserActivityController {

    private final UserActivityService userActivityService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserActivity> getActivity(@PathVariable UUID userId) {
        return ResponseEntity.ok(userActivityService.getActivity(userId));
    }
}
