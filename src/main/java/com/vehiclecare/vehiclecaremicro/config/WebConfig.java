package com.vehiclecare.vehiclecaremicro.config;

import com.vehiclecare.vehiclecaremicro.infrastructure.security.JwtAuthenticationInterceptor;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AdminJwtAuthenticationInterceptor;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final AdminJwtAuthenticationInterceptor adminJwtAuthenticationInterceptor;

    public WebConfig(
            JwtAuthenticationInterceptor jwtAuthenticationInterceptor,
            AdminJwtAuthenticationInterceptor adminJwtAuthenticationInterceptor
    ) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
        this.adminJwtAuthenticationInterceptor = adminJwtAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/admin/**",
                        "/files/**",
                        "/error"
                );

        registry.addInterceptor(adminJwtAuthenticationInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization");
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
