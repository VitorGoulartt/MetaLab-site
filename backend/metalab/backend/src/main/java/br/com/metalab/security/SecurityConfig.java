package br.com.metalab.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, SecurityContextRepository contextoRepository) throws Exception {
        http
                // O CSRF do Spring fica desligado: a proteção vem do cookie SameSite, do CORS restrito e do
                // cabeçalho X-Requested-With exigido em /api/auth e /api/admin (veja CabecalhoAjaxFilter).
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .securityContext(c -> c.securityContextRepository(contextoRepository))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // Sem sessão: 401 (a tela manda para o login). Sem permissão: 403.
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new CabecalhoAjaxFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, "/api/campanhas/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/campanhas/*/participacoes").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/error").permitAll()
                       .requestMatchers("/api/auth/**").authenticated()

                            // Apenas ADMIN e OPERADOR podem executar sorteios.
                        .requestMatchers(HttpMethod.POST, "/api/admin/sorteios")
    .hasAnyRole("ADMIN", "OPERADOR")
   .requestMatchers(HttpMethod.GET, "/api/admin/participantes")
    .hasAnyRole("ADMIN", "OPERADOR", "SUPORTE")


   .requestMatchers(
    HttpMethod.GET,
    "/api/admin/campanhas/*/participantes-elegiveis"
).hasAnyRole("ADMIN", "OPERADOR")
.requestMatchers(HttpMethod.POST, "/api/admin/campanhas")
.hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/admin/campanhas/*")
.hasRole("ADMIN")
// Demais rotas administrativas continuam exigindo autenticação.
.requestMatchers("/api/admin/usuarios/**").hasRole("ADMIN")
.requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().denyAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${metalab.cors.origens}") String origens) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origens.trim().split("\\s*,\\s*")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true); // o navegador envia o cookie de sessão
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", config);
        return fonte;
    }
}
