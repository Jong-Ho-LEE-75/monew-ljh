package com.monew.domain.interest.service;

import com.monew.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;

    // TODO: create(이름 유사도 80% 검증 포함), list, subscribe, unsubscribe 구현
}
