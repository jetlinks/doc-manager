package org.jetlinks.docs.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zyl
 * @date 17/8/2023
 */
@ConfigurationProperties(prefix = "jetlinks.docs.yuque")
@Getter
@Setter
public class YuQueParamsConfig {
    //文档标题
    private String title;

    //文档slug
    private String slug;

    //文档格式
    private String format;

    private String force_asl;

    //文档id
    private String id;

    //文档token
    private String token;

}
