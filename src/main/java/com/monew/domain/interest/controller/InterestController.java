package com.monew.domain.interest.controller;

import com.monew.domain.interest.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestService interestService;

    // TODO: 관심사 CRUD + 구독 엔드포인트 구현
}
