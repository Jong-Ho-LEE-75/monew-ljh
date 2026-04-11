package com.monew.domain.article.collector.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.monew.domain.article.collector.CollectedArticle;
import com.monew.domain.article.collector.config.NewsCollectionProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HankyungRssClientTest {

    private final HankyungRssClient client = new HankyungRssClient(
        null,
        new NewsCollectionProperties(null, List.of())
    );

    @Test
    void RSS_XML_파싱_정상() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Hankyung</title>
                <item>
                  <title><![CDATA[Spring 6 출시]]></title>
                  <link>https://www.hankyung.com/article/1</link>
                  <description><![CDATA[Spring 6가 정식 출시되었습니다.]]></description>
                  <pubDate>Wed, 10 Apr 2026 09:00:00 +0900</pubDate>
                </item>
                <item>
                  <title>Java 25</title>
                  <link>https://www.hankyung.com/article/2</link>
                  <description>Java 25 LTS</description>
                  <pubDate>Wed, 10 Apr 2026 10:00:00 +0900</pubDate>
                </item>
              </channel>
            </rss>
            """;

        List<CollectedArticle> result = client.parseRss(xml);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Spring 6 출시");
        assertThat(result.get(0).summary()).isEqualTo("Spring 6가 정식 출시되었습니다.");
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.hankyung.com/article/1");
        assertThat(result.get(0).source()).isEqualTo("HANKYUNG");
        assertThat(result.get(1).title()).isEqualTo("Java 25");
    }

    @Test
    void 링크_없는_항목은_스킵() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>링크 없음</title>
                  <description>desc</description>
                </item>
              </channel>
            </rss>
            """;

        assertThat(client.parseRss(xml)).isEmpty();
    }

    @Test
    void 깨진_XML은_빈_리스트() {
        assertThat(client.parseRss("<not xml")).isEmpty();
    }

    @Test
    void DOCTYPE_포함시_차단() {
        String xml = """
            <?xml version="1.0"?>
            <!DOCTYPE foo [<!ENTITY x "hack">]>
            <rss><channel><item><link>http://x</link></item></channel></rss>
            """;

        assertThat(client.parseRss(xml)).isEmpty();
    }

    @Test
    void fetch_HTTP_응답_파싱() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <item>
                  <title>헤드라인</title>
                  <link>https://www.hankyung.com/article/99</link>
                  <description>desc</description>
                  <pubDate>Wed, 10 Apr 2026 09:00:00 +0900</pubDate>
                </item>
              </channel>
            </rss>
            """;

        server.expect(requestTo("https://feed.custom/hk"))
            .andRespond(withSuccess(xml, MediaType.APPLICATION_XML));

        NewsCollectionProperties properties = new NewsCollectionProperties(
            null,
            List.of(new NewsCollectionProperties.RssFeed("HANKYUNG", "https://feed.custom/hk"))
        );
        HankyungRssClient httpClient = new HankyungRssClient(builder.build(), properties);

        List<CollectedArticle> result = httpClient.fetch("query");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sourceUrl()).isEqualTo("https://www.hankyung.com/article/99");
        server.verify();
    }

    @Test
    void fetch_빈_본문_빈_리스트() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://www.hankyung.com/feed/all-news"))
            .andRespond(withSuccess("", MediaType.APPLICATION_XML));

        HankyungRssClient httpClient = new HankyungRssClient(
            builder.build(),
            new NewsCollectionProperties(null, List.of())
        );

        assertThat(httpClient.fetch("q")).isEmpty();
    }

    @Test
    void fetch_설정된_RSS_없으면_기본_피드_사용() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://www.hankyung.com/feed/all-news"))
            .andRespond(withSuccess("<rss><channel/></rss>", MediaType.APPLICATION_XML));

        HankyungRssClient httpClient = new HankyungRssClient(
            builder.build(),
            new NewsCollectionProperties(
                null,
                List.of(new NewsCollectionProperties.RssFeed("OTHER", "https://x"))
            )
        );

        assertThat(httpClient.fetch("q")).isEmpty();
        server.verify();
    }

    @Test
    void sourceName_상수() {
        assertThat(client.sourceName()).isEqualTo("HANKYUNG");
    }
}
