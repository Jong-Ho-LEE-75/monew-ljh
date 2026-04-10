package com.monew.domain.article.service;

import com.monew.domain.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    // TODO: list(관심사 필터 + 커서 페이지네이션), view(조회수 unique 집계), softDelete 구현
}
