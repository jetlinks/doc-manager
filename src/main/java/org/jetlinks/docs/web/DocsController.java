package org.jetlinks.docs.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.docs.entity.PullRequestParam;
import org.jetlinks.docs.service.DocsService;
import org.jetlinks.docs.service.YuQueDocumentUpdateService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 文档控制器.
 *
 * @author zhangji 2023/2/22
 */
@RestController
@RequestMapping("/docs")
@AllArgsConstructor
@Slf4j
public class DocsController {

    private final DocsService service;

    private final YuQueDocumentUpdateService yuQueService;

    /**
     * 手动获取指定时间段的PR
     * 按PR标题分类，例如提交了多个仓库，合并在一起。然后分别提供链接
     * 按提交信息中的类型或者具体功能分类
     */
    @PostMapping("/pull/request")
    public Mono<String> queryAndSavePullRequest(@RequestParam(required = false) String mode,
                                                @RequestBody Mono<PullRequestParam> mono) {
        return mono.flatMap(param -> service.queryAndBuildPullRequest(mode, param, yuQueService));

    }

}
