package br.com.academiadigital.backend.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/health/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        .requestMatchers("/api/v1/usuarios/**")
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/cursos/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR",
                                "ALUNO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/cursos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/cursos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/cursos/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/aulas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR",
                                "ALUNO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/aulas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/aulas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/aulas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/avaliacoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR",
                                "ALUNO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/avaliacoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/avaliacoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/avaliacoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/questoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR",
                                "ALUNO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/questoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/questoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/questoes/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR"
                        )
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/trilhas/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "PROFESSOR",
                                "ALUNO"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/trilhas/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/trilhas/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/trilhas/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/trilhas/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/progressos/aulas/*/concluir"
                        )
                        .hasRole("ALUNO")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/progressos/aulas/*/conclusao"
                        )
                        .hasRole("ALUNO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/progressos/cursos/**"
                        )
                        .hasRole("ALUNO")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/matriculas/minhas"
                        )
                        .hasRole("ALUNO")

                        .requestMatchers(
                                "/api/v1/matriculas/**"
                        )
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                );

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
