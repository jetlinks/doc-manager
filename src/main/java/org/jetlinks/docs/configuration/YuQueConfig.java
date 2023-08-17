package org.jetlinks.docs.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 设置请求信息
 *
 * @author zyl
 * @date 11/8/2023
 */
@Configuration
public class YuQueConfig {
    private final YuQueParamsConfig config;

    public YuQueConfig(YuQueParamsConfig config) {
        this.config = config;
    }

    @Bean
    public WebClient yuQueWebClient() {
        return WebClient.builder()
                .baseUrl("https://hanta.yuque.com/api/v2/repos/px7kg1/vwoix4/docs/" + config.getId())
                .defaultHeader("X-Auth-Token", config.getToken())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configure -> configure
                                .defaultCodecs()
                                .maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }
}
