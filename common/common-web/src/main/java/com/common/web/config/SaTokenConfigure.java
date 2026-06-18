package com.common.web.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.util.SaResult;
import com.common.core.exception.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证 配置类
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico")
                .setAuth(obj -> {
                    // 校验 Same-Token 身份凭证 可简化为：SaSameUtil.checkCurrentRequestToken();
                    SaSameUtil.checkCurrentRequestToken();
                })
                .setError(e -> new SaResult()
                        .setCode(ErrorCode.NO_AUTH_ERROR.getCode())
                        .setMsg("Same-Token 验证失败！")
                        .setData(null));
    }

}
