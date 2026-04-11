package com.monew.domain.article.collector.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.monew.domain.article.collector.CollectedArticle;
import com.monew.domain.article.collector.config.NewsCollectionProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

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
}
