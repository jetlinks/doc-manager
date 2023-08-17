package org.jetlinks.docs.client.content.pullrequest;

import lombok.extern.slf4j.Slf4j;
import org.jetlinks.docs.enums.BuilderType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 生成PR文档-根据功能分组.
 *
 * @author zhangji 2023/2/22
 */
@Slf4j
@Component
public class PullRequestScopeBuilder extends AbstractPullRequestBuilder {

    @Override
    public String getType() {
        return BuilderType.PULL_REQUEST_SCOPE.name();
    }

    @Override
    protected String doGroup(MarkdownInfo markdownInfo) {
//        String scope = markdownInfo.getTitle().getScope();
        String scope = markdownInfo.getRepoName();
        return scope == null ? "" : scope;
    }


    @Override
    protected Collection<MarkdownInfo> parseMarkdownInfo(List<MarkdownInfo> infoList) {
        // 按名称+时间去重
        Map<String, MarkdownInfo> cache = new HashMap<>();
        for (MarkdownInfo markdownInfo : infoList) {
            if (markdownInfo == null || markdownInfo.getMergedAt() == null) {
                continue;
            }
            cache.compute(CommitType.parse(markdownInfo.getTitle().getType()).getText(), (key, old) -> {
                if (old == null) {
                    old = markdownInfo;
                } else {
                    // 相同提交信息，一天以内的PR都视为同一个分支
//                    if (old.getMergedAt().isBefore(markdownInfo.getMergedAt().plusDays(1)) &&
//                            old.getMergedAt().isAfter(markdownInfo.getMergedAt().minusDays(1))) {
                    // 合并分支信息
                    old.merge(markdownInfo);
//                    }
                }
                return old;
            });
        }

        return cache.values();
    }

    @Override
    protected String doBuildMarkdown(MarkdownInfo markdownInfo) {
        StringBuilder markdown = new StringBuilder();

        markdownInfo.getTypeInfo().forEach((type, detailList) -> {
            markdown.append("**")
//                    .append(CommitType.parse(type).getText());
                    .append(type)
                    .append("**");

            String currentTitle = null;
            for (Detail detail : detailList) {
                if (!detail.getDescription().equals(currentTitle)) {
                    currentTitle = detail.getDescription();
                    markdown
                            .append("\n")
                            .append("- ")
                            .append(currentTitle);
                }

                markdown.append(" [#").append(detail.getNumber()).append("]")
                        .append("(").append(detail.getHtmlUrl()).append(") ");
            }
            markdown.append("\n");
        });
        return markdown.toString();
    }

}
