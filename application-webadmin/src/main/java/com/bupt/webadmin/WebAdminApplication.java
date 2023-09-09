package com.bupt.webadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 应用服务启动类。
 *
 * @author zzh
 * @date 2023-08-10
 */
@EnableAsync
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan("com.bupt")
public class WebAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebAdminApplication.class, args);
		System.out.println("(♥◠‿◠)ﾉﾞ  orange-demo-flowable-service启动成功   ლ(´ڡ`ლ)ﾞ");
	}
}
