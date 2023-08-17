package org.jetlinks.docs.service;

import org.jetlinks.docs.configuration.PullRequestParamConfig;
import org.jetlinks.docs.entity.PullRequestParam;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.jetlinks.docs.client.content.pullrequest.AbstractPullRequestBuilder.DATE_TIME_FORMATTER;

/**
 * @author zyl
 * @date 14/8/2023
 */
@Component
public class ScheduledTask {

    private final PullRequestParamConfig config;

    public ScheduledTask(PullRequestParamConfig config) {
        this.config = config;
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60)//一小时上传一次
    public void update() {
        DocsService docsService = SpringUtils.getBean(DocsService.class);
        YuQueDocumentUpdateService yuQueService = SpringUtils.getBean(YuQueDocumentUpdateService.class);
        //设置更新参数
        PullRequestParam param = new PullRequestParam();
        param.setMergeStart(LocalDateTime.parse(config.getStartTime(), DATE_TIME_FORMATTER));
        String mode = config.getMode();
        //更新
        docsService
                .queryAndBuildPullRequest(mode, param, yuQueService)
                .subscribe();
    }
}
