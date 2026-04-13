package com.monew.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MDCLoggingInterceptor mdcLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcLoggingInterceptor)
                .addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // SPA 포워딩: 정적 리소스가 없으면 index.html 반환
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // API, actuator, swagger 경로는 포워딩하지 않음
                        if (resourcePath.startsWith("api/")
                                || resourcePath.startsWith("actuator/")
                                || resourcePath.startsWith("swagger")
                                || resourcePath.startsWith("v3/")) {
                            return null;
                        }
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
