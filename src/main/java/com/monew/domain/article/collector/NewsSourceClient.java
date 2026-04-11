package com.monew.domain.article.collector;

import java.util.List;

public interface NewsSourceClient {

    String sourceName();

    List<CollectedArticle> fetch(String query);
}
