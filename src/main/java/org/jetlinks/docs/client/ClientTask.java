package org.jetlinks.docs.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.docs.client.request.RequestCommand;
import org.jetlinks.docs.entity.PullRequestParam;
import org.jetlinks.docs.service.YuQueDocumentUpdaterService;
import org.jetlinks.docs.utils.MarkdownFileUtils;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档查询任务.
 *
 * @author zhangji 2023/2/22
 */
@Slf4j
@AllArgsConstructor

public class ClientTask {

    // api请求命令
    private final RequestCommand requestCommand;

    /**
     * 执行请求命令，并且生成文档
     *
     * @param param 请求参数
     * @return 文档
     */
    public Mono<String> apply(PullRequestParam param, YuQueDocumentUpdaterService yuQueService) {
        //语雀api 请求参数
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("title", yuQueService.getConfig().getTitle());
        jsonBody.put("slug", yuQueService.getConfig().getSlug());
        jsonBody.put("format", yuQueService.getConfig().getFormat());
        jsonBody.put("_force_asl", yuQueService.getConfig().getForce_asl());

        return requestCommand
                .apply(param)
                .doOnNext(markdown -> {
                    log.info(markdown);
                    // 生成临时文件
                    MarkdownFileUtils.writeToFile(markdown, param.getMergeStart(), param.getMergeEnd());

                    //上传语雀
                    jsonBody.put("body", markdown);
                    yuQueService.updateDocument(jsonBody);
                });
    }

}
