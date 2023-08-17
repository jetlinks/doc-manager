package org.jetlinks.docs;

import org.jetlinks.docs.service.ScheduledTask;
import org.jetlinks.docs.service.SpringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 输入描述.
 *
 * @author zhangji 2023/2/21
 */
@SpringBootApplication(scanBasePackages = "org.jetlinks.docs")
@EnableScheduling
public class JetlinksDocsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JetlinksDocsApplication.class);

        //定时更新
        ScheduledTask scheduledTask = SpringUtils.getBean(ScheduledTask.class);
        scheduledTask.update();
    }
}
