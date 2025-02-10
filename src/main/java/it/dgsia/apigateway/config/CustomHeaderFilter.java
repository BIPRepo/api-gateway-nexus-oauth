package it.dgsia.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import it.dgsia.apigateway.exception.UnauthorizedAccessException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomHeaderFilter extends AbstractGatewayFilterFactory<CustomHeaderFilter.Config> {

	public static final String CACHE_USERS_KEY = "users";
	private static final String X_CUSTOM_HEADER = "X-Keycloak-Sec-Auth";
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String AUTH_PREFIX = "Bearer ";
	private static final String ACCESSO_NEGATO_UTENTE_NON_ATTIVO = "Accesso negato: utente non attivo";
	private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

	public CustomHeaderFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
		super(Config.class);
		this.authorizedClientRepository = authorizedClientRepository;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> exchange.getPrincipal().flatMap(principal -> {

			if (principal instanceof JwtAuthenticationToken) {
				JwtAuthenticationToken jwtAuthToken = (JwtAuthenticationToken) principal;
				Jwt jwt = jwtAuthToken.getToken();
				String jwtStr = jwt.getSubject() + ":" + jwt.getTokenValue();

				ServerWebExchange modifiedExchange = exchange.mutate().request(request -> request.headers(headers -> {
					headers.add(X_CUSTOM_HEADER, jwtStr);
					headers.add(AUTHORIZATION_HEADER, AUTH_PREFIX + jwtStr);
				})).build();

				return chain.filter(modifiedExchange);

			} else if (principal instanceof OAuth2AuthenticationToken) {
				OAuth2AuthenticationToken jwtAuthToken = (OAuth2AuthenticationToken) principal;
				return authorizedClientRepository
						.loadAuthorizedClient(jwtAuthToken.getAuthorizedClientRegistrationId(), jwtAuthToken, exchange)
						.flatMap(authorizedClient -> {
							String jwt = authorizedClient.getAccessToken().getTokenValue();
							ServerWebExchange modifiedExchange = exchange.mutate()
									.request(request -> request.headers(headers -> {
										headers.add(X_CUSTOM_HEADER, jwt);
										headers.add("Authorization", "Bearer " + jwt);
									})).build();

							return chain.filter(modifiedExchange);
						});
			} else {
				// Caso predefinito: nessun JWT
				return Mono.error(new UnauthorizedAccessException(ACCESSO_NEGATO_UTENTE_NON_ATTIVO));
			}
		});
	}

	public static class Config {
		// Configurazione opzionale
	}
}