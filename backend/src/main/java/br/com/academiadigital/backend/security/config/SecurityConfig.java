package br.com.academiadigital.backend.security.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import br.com.academiadigital.backend.security.jwt.JwtAuthenticationFilter;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.http.HttpMethod;

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

            .authorizeHttpRequests(authorize ->
                    authorize
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