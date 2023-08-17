package org.jetlinks.docs.service;

import lombok.Getter;
import org.jetlinks.docs.configuration.YuQueParamsConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zyl
 * @date 11/8/2023
 */
@Service
@Getter
public class YuQueDocumentUpdateService {
    private final WebClient yuqueWebClient;

    private final YuQueParamsConfig config;

    private final HashMap<String,Object> params = new HashMap<>();

    public YuQueDocumentUpdateService(WebClient yuqueWebClient, YuQueParamsConfig config) {
        this.yuqueWebClient = yuqueWebClient;
        this.config = config;
    }

    public Mono<Map> updateDocument(Map<String, Object> params) {
        //语雀api 请求参数
        params.put("title", config.getTitle());
        params.put("slug", config.getSlug());
        params.put("format", config.getFormat());
        params.put("_force_asl", config.getForce_asl());
        return yuqueWebClient
                .put()
                .body(BodyInserters.fromValue(params))
                .retrieve()
                .bodyToMono(Map.class);
    }
}
