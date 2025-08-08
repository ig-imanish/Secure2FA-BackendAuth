package com.bristoHQ.securetotp.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.bristoHQ.securetotp.security.CustomSuccessHandler;
import com.bristoHQ.securetotp.security.CustomerUserDetailsService;
import com.bristoHQ.securetotp.security.RateLimitFilter;
import com.bristoHQ.securetotp.security.jwt.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

        @Autowired
        private JwtAuthenticationFilter jwtAuthenticationFilter;
        @Autowired
        private CustomerUserDetailsService customerUserDetailsService;
        @Autowired
        private RateLimitFilter rateLimitFilter;

        @Autowired
        private CustomSuccessHandler successHandler;

        @Bean
        public CorsFilter corsFilter() {
                CorsConfiguration corsConfig = new CorsConfiguration();

                corsConfig.setAllowedOriginPatterns(List.of(
                                "https://bristohq.github.io",
                                "https://securetotp.netlify.app",
                                "http://localhost:5500",
                                "http://127.0.0.1:5500",
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://localhost:5173",
                                "http://127.0.0.1:5173",

                                "http://localhost:5174",
                                "http://127.0.0.1:5174",
                                "https://securetotp-service.onrender.com"));

                corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(
                                List.of("Authorization", "Content-Type", "ngrok-skip-browser-warning", "User-Agent"));
                corsConfig.setAllowCredentials(true); // Allow cookies or auth headers

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", corsConfig);
                return new CorsFilter(source);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/**", "/css/**", "/js/**", "/images/**",
                                                                "/error",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html", "/api/v1/users/byToken",
                                                                "/login/oauth2/code/google",
                                                                "/api/v1/public/users/**")
                                                .permitAll()
                                                .requestMatchers("/api/v1/auth/public").permitAll()
                                                .requestMatchers("/api/**").authenticated()

                                                .requestMatchers("/api/v1/users/**")
                                                .hasAnyAuthority("USER", "ADMIN", "SUPER_ADMIN")

                                                .requestMatchers("/api/v1/admins/**")
                                                .hasAnyAuthority("ADMIN", "SUPER_ADMIN")

                                                .requestMatchers("/api/v1/superadmins/**").hasAnyAuthority(
                                                                "SUPER_ADMIN")
                                                .anyRequest().authenticated()

                                ).oauth2Login(login -> login
                                                .successHandler(successHandler)
                                                .permitAll())

                                // Handle unauthorized requests with JSON response instead of redirect
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(unauthorizedHandler()))

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
                var authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
                authManagerBuilder
                                .userDetailsService(customerUserDetailsService)
                                .passwordEncoder(passwordEncoder());
                return authManagerBuilder.build();
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationEntryPoint unauthorizedHandler() {
                return (request, response, error) -> {
                        response.setContentType("application/json");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write(
                                        "{\"error\": \"Unauthorized\", \"message\": \"You need to log in first.\"}");
                };
        }
}
