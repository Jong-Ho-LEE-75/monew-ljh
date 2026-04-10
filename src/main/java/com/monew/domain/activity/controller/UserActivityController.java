package com.monew.domain.activity.controller;

import com.monew.domain.activity.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-activities")
public class UserActivityController {

    private final UserActivityService userActivityService;

    // TODO: GET /{userId} 사용자 활동 내역 조회
}
