package com.monew.domain.article.controller;

import com.monew.domain.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    // TODO: GET 목록 / GET 상세(조회수 증가) / DELETE(논리 삭제)
}
