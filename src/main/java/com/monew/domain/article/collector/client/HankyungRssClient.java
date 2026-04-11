package com.monew.domain.article.collector.client;

import com.monew.domain.article.collector.CollectedArticle;
import com.monew.domain.article.collector.NewsSourceClient;
import com.monew.domain.article.collector.config.NewsCollectionProperties;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Slf4j
@Component
@RequiredArgsConstructor
public class HankyungRssClient implements NewsSourceClient {

    private static final String SOURCE_NAME = "HANKYUNG";
    private static final String DEFAULT_FEED = "https://www.hankyung.com/feed/all-news";
    private static final DateTimeFormatter PUB_DATE_FORMAT = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final RestClient newsRestClient;
    private final NewsCollectionProperties properties;

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<CollectedArticle> fetch(String query) {
        String feedUrl = resolveFeedUrl();
        String body = newsRestClient.get()
            .uri(feedUrl)
            .retrieve()
            .body(String.class);

        if (body == null || body.isBlank()) {
            return List.of();
        }

        return parseRss(body);
    }

    private String resolveFeedUrl() {
        if (properties.rss() == null) {
            return DEFAULT_FEED;
        }
        return properties.rss().stream()
            .filter(feed -> SOURCE_NAME.equalsIgnoreCase(feed.source()))
            .findFirst()
            .map(NewsCollectionProperties.RssFeed::url)
            .orElse(DEFAULT_FEED);
    }

    List<CollectedArticle> parseRss(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            NodeList items = doc.getElementsByTagName("item");
            List<CollectedArticle> result = new ArrayList<>(items.getLength());
            for (int i = 0; i < items.getLength(); i++) {
                Node node = items.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element item = (Element) node;
                String title = textOf(item, "title");
                String link = textOf(item, "link");
                String description = textOf(item, "description");
                String pubDate = textOf(item, "pubDate");
                if (link == null || link.isBlank()) {
                    continue;
                }
                result.add(new CollectedArticle(
                    SOURCE_NAME,
                    link.trim(),
                    stripCdata(title),
                    stripCdata(description),
                    parsePubDate(pubDate)
                ));
            }
            return result;
        } catch (Exception e) {
            log.warn("RSS 파싱 실패 source={}", SOURCE_NAME, e);
            return List.of();
        }
    }

    private static String textOf(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0) {
            return null;
        }
        return list.item(0).getTextContent();
    }

    private static String stripCdata(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("<![CDATA[", "")
            .replace("]]>", "")
            .trim();
    }

    private static Instant parsePubDate(String pubDate) {
        if (pubDate == null || pubDate.isBlank()) {
            return Instant.now();
        }
        try {
            return ZonedDateTime.parse(pubDate.trim(), PUB_DATE_FORMAT).toInstant();
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
