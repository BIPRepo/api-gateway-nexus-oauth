package it.dgsia.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CorsSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager=true)
public class SecurityConfig {

    @Value("${gateway-api.authentication.enabled:true}")
    private boolean authEnabled;

    @Value("${gateway-api.cors.allowedOrigin:''}")
    private String allowedOrigin;

    @Value("${gateway-api.cors.enabled:false}")
    private boolean corsEnabled;

    private final JwtAuthConverter jwtAuthConverter;

    public SecurityConfig(JwtAuthConverter jwtAuthConverter){
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {

        http.csrf(CsrfSpec::disable)
                .authorizeExchange((auth) -> auth
                        .pathMatchers(HttpMethod.GET, "/actuator/**").permitAll());

        if (corsEnabled) {
            http.cors(Customizer.withDefaults());
        } else {
            http.cors(CorsSpec::disable);
        }

        if (authEnabled) {
            http.authorizeExchange((authorize) -> authorize
                    .anyExchange().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                    		.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                    .oauth2Login(Customizer.withDefaults());
        } else {
        	http.authorizeExchange((authorize) -> authorize.anyExchange().permitAll());
        }
        
         // Abilita il login automatico via OAuth2
        
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList(allowedOrigin));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.stream(HttpMethod.values()).map(HttpMethod::name).toList());
        config.setExposedHeaders(Arrays.asList(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
