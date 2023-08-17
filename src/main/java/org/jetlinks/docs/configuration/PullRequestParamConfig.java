package org.jetlinks.docs.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zyl
 * @date 14/8/2023
 */
@ConfigurationProperties(prefix = "jetlinks.docs.param")
@Getter
@Setter
public class PullRequestParamConfig {
    //PR合并起始时间
    private String startTime;

    //输入PR合并截止时间
    private String endTime;

    //输入文档格式（scope：以功能分组；type：以类型分组）
    private String mode;
}
