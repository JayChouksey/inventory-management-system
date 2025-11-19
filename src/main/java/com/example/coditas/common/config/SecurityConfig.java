package com.example.coditas.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtFilter jwtFilter;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // secured endpoints
                        .requestMatchers("/api/v1/factories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,"/api/v1/users").hasAnyRole("ADMIN", "PLANT_HEAD")
                        .requestMatchers(HttpMethod.GET,"/api/v1/users").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/v1/users/dashboard").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/api/v1/users/dealers").hasAnyRole("ADMIN")
                        .requestMatchers("/api/v1/users/search").hasAnyRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "PLANT_HEAD", "CHIEF_SUPERVISOR", "WORKER", "CENTRAL_OFFICE_HEAD", "DEALER", "CUSTOMER")
                        .requestMatchers("/api/v1/users/factory/**").hasAnyRole("ADMIN", "PLANT_HEAD")
                        .requestMatchers("/api/v1/users/bay/**").hasAnyRole("ADMIN", "PLANT_HEAD")

                        .requestMatchers("/api/v1/tools/**").hasAnyRole("ADMIN","PLANT_HEAD","CHIEF_SUPERVISOR","WORKER")

                        .requestMatchers(HttpMethod.POST,"/api/v1/tool-requests").hasRole("WORKER")
                        .requestMatchers(HttpMethod.POST,"/api/v1/tool-requests/my").hasRole("WORKER")
                        .requestMatchers(HttpMethod.GET,"/api/v1/tool-requests").hasAnyRole("PLANT_HEAD","CHIEF_SUPERVISOR")
                        .requestMatchers("/api/v1/tool-requests/approve").hasAnyRole("PLANT_HEAD","CHIEF_SUPERVISOR")
                        .requestMatchers("/api/v1/tool-requests/reject").hasAnyRole("PLANT_HEAD","CHIEF_SUPERVISOR")
                        .requestMatchers(HttpMethod.GET,"/api/v1/tool-requests/**").hasAnyRole("PLANT_HEAD","CHIEF_SUPERVISOR","WORKER")

                        .requestMatchers(HttpMethod.GET,"/api/v1/tool-requests/**").hasAnyRole("PLANT_HEAD","CHIEF_SUPERVISOR","WORKER")

                        // any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
