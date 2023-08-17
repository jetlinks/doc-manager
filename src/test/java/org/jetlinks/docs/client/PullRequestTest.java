package org.jetlinks.docs.client;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.docs.client.content.DocsContentBuilder;
import org.jetlinks.docs.client.content.pullrequest.PullRequestScopeBuilder;
import org.jetlinks.docs.entity.PullRequestParam;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 输入描述.
 *
 * @author zhangji 2023/2/22
 */
@Slf4j
public class PullRequestTest {

    @Test
    void buildDocsContentTest() {
        PullRequestParam param = new PullRequestParam();

        try (InputStream inputStream = new ClassPathResource("pull-request-response.json").getInputStream()) {
            String json = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            JSONArray jsonArray = JSONArray.parseArray(json);

            DocsContentBuilder docsContentBuilder1 = new PullRequestScopeBuilder();
            docsContentBuilder1
                    .buildMarkdown(param, Flux.just(jsonArray))
                    .doOnNext(doc -> log.info("doc:\n{}", doc))
                    .as(StepVerifier::create)
                    .expectNextCount(1)
                    .verifyComplete();

            DocsContentBuilder docsContentBuilder2 = new PullRequestScopeBuilder();
            docsContentBuilder2
                    .buildMarkdown(param, Flux.just(jsonArray))
                    .doOnNext(doc -> log.info("doc:\n{}", doc))
                    .as(StepVerifier::create)
                    .expectNextCount(1)
                    .verifyComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void yuQue() throws InterruptedException {
//        String apiUrl = "https://hanta.yuque.com/api/v2/repos/px7kg1/vwoix4/docs/135984390?title=pr&slug=yyzh6gux3xl59i5p&format=markdown&_force_asl=true";
        String apiUrl = "https://hanta.yuque.com/api/v2/repos/px7kg1/vwoix4/docs/135984390";
        String accessToken = "628zLgRuN9ZhdKGVKPe59mqAkkDRXE462YHh3ETJ";

        WebClient webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("X-Auth-Token", accessToken)
                .build();

        webClient.put()
                .uri(uriBuilder -> uriBuilder.path("title=pr&slug=yyzh6gux3xl59i5p&format=markdown&_force_asl=true&body=666").build())
                .body(BodyInserters.fromValue("6666666"))
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe();
        Thread.sleep(10000);
    }
}
