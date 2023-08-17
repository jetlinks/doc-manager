package org.jetlinks.docs.service;

import lombok.Getter;
import org.jetlinks.docs.configuration.YuQueParamsConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * @author zyl
 * @date 11/8/2023
 */
@Service
@Getter
public class YuQueDocumentUpdaterService {
    private final WebClient yuqueWebClient;

    private final YuQueParamsConfig config;

    public YuQueDocumentUpdaterService(WebClient yuqueWebClient, YuQueParamsConfig config) {
        this.yuqueWebClient = yuqueWebClient;
        this.config = config;
    }

    public void updateDocument(Map<String, Object> jsonBody) {
        yuqueWebClient
                .put()
                .body(BodyInserters.fromValue(jsonBody))
                .retrieve()
                .bodyToMono(Map.class)
                .subscribe();
    }
}
