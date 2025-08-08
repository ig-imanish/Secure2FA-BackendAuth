package com.bristoHQ.securetotp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bristoHQ.securetotp.interceptors.AnalyticsInterceptor;

@Configuration
public class AnalyticsWebConfig implements WebMvcConfigurer {

    @Autowired
    private AnalyticsInterceptor analyticsInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(analyticsInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/static/**", // Static resources
                        "/css/**",
                        "/js/**",
                        "/assets/**",
                        "/img/**",
                        "/images/**",
                        "/api/v1/auth/login", // Exclude login endpoint to avoid spam
                        "/api/v1/auth/register", // Exclude register endpoint
                        "/api/v1/auth/verify-otp", // Exclude OTP verification
                        "/api/v1/auth/resend-otp", // Exclude OTP resend
                        "/api/v1/auth/reset-password", // Exclude password reset
                        "/swagger-ui/**", // Swagger UI
                        "/v3/api-docs/**", // API documentation
                        "/webjars/**", // Web jars
                        "/favicon.ico" // Favicon
                );
    }
}
