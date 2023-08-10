package org.jetlinks.docs.client.content.pullrequest;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetlinks.docs.client.content.DocsContentBuilder;
import org.jetlinks.docs.entity.PullRequestInfo;
import org.jetlinks.docs.entity.PullRequestParam;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * markdown生成器.
 *
 * @author zhangji 2023/2/22
 */
@Slf4j
@Component
public abstract class AbstractPullRequestBuilder implements DocsContentBuilder {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<String> buildMarkdown(PullRequestParam param,
                                      Flux<JSONArray> jsonArrayFlux) {
        return jsonArrayFlux
                .flatMapIterable(PullRequestInfo::of)
//                .flatMapIterable(this::parsePullRequestInfo)
                .mapNotNull(MarkdownInfo::of)
                .filter(markdownInfo -> {
                    LocalDateTime mergeAt = markdownInfo.getMergedAt();
                    // 过滤没有合并的PR（合并时间为空，表示未合并）
                    if (mergeAt == null) {
                        return false;
                    }
                    // 按指定时间过滤
                    boolean filter = true;
                    if (param.getMergeStart() != null) {
                        filter = param.getMergeStart().isBefore(mergeAt);
                    }
                    if (param.getMergeEnd() != null) {
                        filter = filter && param.getMergeEnd().isAfter(mergeAt);
                    }
                    return filter;
                })
                .groupBy(MarkdownInfo::getRepoName)
                .flatMap(repoGroup -> repoGroup
                        .groupBy(this::doGroup)
                        .flatMap(group -> group
                                .sort(Comparator.comparing(MarkdownInfo::getMergedAt).reversed())
                                .collectList()
                                .map(this::parseMarkdownInfo)
                                .flatMapIterable(Function.identity())
                                .map(this::doBuildMarkdown)
                                .collectList()
                                .map(markdownList -> getGroupTitle(group.key()) + String.join("\n", markdownList)))
                        .collectList()
                        .map(markdownList -> getRepoGroupTitle(repoGroup.key()) + String.join("\n", markdownList)))
                .collectList()
                .map(markdownList -> getHead() + String.join("\n", markdownList));
    }

    /**
     * 自定义标题
     *
     * @return 标题部分
     */
    protected String getHead() {
        return "";
    }

    protected String getRepoGroupTitle(String title) {
        return "# " + title + "\n";
    }

    protected String getGroupTitle(String title) {
        return "## " + title + "\n";
    }

    protected abstract String doGroup(MarkdownInfo markdownInfo);

    protected abstract Collection<MarkdownInfo> parseMarkdownInfo(List<MarkdownInfo> pullRequestInfoList);

    /**
     * 自定义格式
     *
     * @param markdownInfo 文档信息
     * @return 文档
     */
    protected abstract String doBuildMarkdown(MarkdownInfo markdownInfo);

    @Getter
    @Setter
    public static class MarkdownInfo {

        private Title title;

        private PullRequestInfo.User user;

        private LocalDateTime mergedAt;

        private String repoName;

        private Map<String, List<Detail>> scopeInfo = new HashMap<>();

        private Map<String, List<Detail>> typeInfo = new HashMap<>();

        public static MarkdownInfo of(PullRequestInfo pullRequestInfo) {
            MarkdownInfo markdownInfo = new MarkdownInfo();
            Title title = Title.of(pullRequestInfo.getTitle());
            if (title == null || !StringUtils.hasText(title.getType())) {
                return null;
            }

            markdownInfo.setTitle(title);
            markdownInfo.setUser(pullRequestInfo.getUser());
            markdownInfo.setMergedAt(pullRequestInfo.getMergedAt());
            markdownInfo.setRepoName(pullRequestInfo.getHead().getRepo().getName());
            Detail detail = Detail.of(pullRequestInfo).with(title);
            addDetail(markdownInfo.getScopeInfo(), detail.getScope(), Collections.singletonList(detail));
            addDetail(markdownInfo.getTypeInfo(), detail.getType(), Collections.singletonList(detail));

            return markdownInfo;
        }

        public MarkdownInfo merge(MarkdownInfo markdownInfo) {
            if (markdownInfo.getScopeInfo() != null) {
                markdownInfo
                        .getScopeInfo()
                        .forEach((key, value) -> addDetail(this.getScopeInfo(), key, value));
            }

            if (markdownInfo.getTypeInfo() != null) {
                markdownInfo
                        .getTypeInfo()
                        .forEach((key, value) -> addDetail(this.getTypeInfo(), key, value));
            }

            return this;
        }

        private static void addDetail(Map<String, List<Detail>> scopeInfo,
                                      String key,
                                      List<Detail> detail) {
            if (scopeInfo == null) {
                return;
            }
            scopeInfo.compute(key, (scope, detailList) -> {
                if (CollectionUtils.isEmpty(detailList)) {
                    detailList = new ArrayList<>();
                }
                detailList.addAll(detail);

                return detailList;
            });
        }
    }

    @Getter
    @Setter
    public static class Title {

        private String type;

        private String scope;

        private String description;

        /**
         * 解析PR标题
         * 格式：^(feat|fix|test|refactor|docs|style|chore|ci|revert|perf|build)\(.*\):.*$ ]]
         *
         * @param text
         * @return
         */
        public static Title of(String text) {
            try {
                Title title = new Title();
                if (text.contains("(") && text.contains("):")) {
                    title.setType(text.substring(0, text.indexOf("(")));
                    title.setScope(text.substring(text.indexOf("(") + 1, text.indexOf("):")));
                    if (text.indexOf("):") + 2 > 0) {
                        title.setDescription(text.substring(text.indexOf("):") + 2).trim());
                    } else {
                        title.setDescription("");
                    }
                } else if (text.contains(":")) {
                    String[] str = text.split(":");
                    title.setType(str[0].trim());
                    title.setDescription(str[1].trim());
                } else {
                    title.setScope(text);
                    title.setDescription(text);
                }

                return title;
            } catch (Exception e) {
                log.warn("parse title error. text={}", text);
                return null;
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public enum CommitType {

        feat("新功能"),
        fix("bug修复"),
        docs("文档更新"),
        style("代码风格跳转"),
        refactor("重构代码"),
        test("测试代码"),
        perf("性能优化"),
        revert("回滚代码"),
        chore("其他改动");

        private final String text;

        public static CommitType parse(String type) {
            return Arrays
                    .stream(values())
                    .filter(commitType -> commitType.name().equals(type) || type.toLowerCase().contains("revert"))
                    .findAny()
                    .orElse(chore);
        }
    }

    @Getter
    @Setter
    public static class ScopeInfo {

        private String scope;

        private List<Detail> detail;
    }

    @Getter
    @Setter
    public static class TypeInfo {

        private String type;

        private List<Detail> detail;
    }

    @Getter
    @Setter
    public static class Detail {

        private String repoName;

        private String repoUrl;

        private String scope;

        private String type;

        private int number;

        private String htmlUrl;

        private String description;

        public static Detail of(PullRequestInfo pullRequestInfo) {
            Detail detail = new Detail();
            PullRequestInfo.Repo repo = pullRequestInfo.getHead().getRepo();
            detail.setRepoName(repo.getName());
            detail.setRepoUrl(repo.getHtmlUrl());
            detail.setNumber(pullRequestInfo.getNumber());
            detail.setHtmlUrl(pullRequestInfo.getHtmlUrl());
            return detail;
        }

        public Detail with(Title title) {
            setScope(title.getScope());
            setType(title.getType());
            setDescription(title.getDescription());
            return this;
        }
    }
}
