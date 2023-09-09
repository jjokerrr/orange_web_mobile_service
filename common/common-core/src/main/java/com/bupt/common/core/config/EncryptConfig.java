package com.bupt.common.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 目前用于用户密码加密，UAA接入应用客户端的client_secret加密。
 *
 * @author zzh
 * @date 2023-08-10
 */
@Configuration
public class EncryptConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
