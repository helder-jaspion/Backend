package br.com.cdsoft.oauth;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilitário para retornar informações do Token do Usuário.
 * 
 * @author Cléber da Silveira
 *
 */
@Component
public class UserInfoTokenUtil {

	private static final String EMAIL = "email";
	private static final String PREFERRED_USERNAME = "preferred_username";
	private static final String BLANK = "";
	private static final String ONLY_DIGITS = "\\D+";
	private static final String OU = ",OU=";
	private static ObjectMapper OBJECT_MAPPER = null;
	private static final String AUTHORITIES = "authorities";
	private static final String DISTINGUISHED_NAME = "distinguishedName";
	private static final String GIVEN_NAME = "given_name";
	private static final String NAME = "name";
	private static final String REALM_ACCESS = "realm_access";
	private static final String RESOURCE_ACCESS = "resource_access";
	private static Logger LOGGER = LoggerFactory.getLogger(UserInfoTokenUtil.class);


	/**
	 * Método responsável por retornar o valor do Token.
	 * 
	 * @param key
	 *            chave do token.
	 * @return Valor do Token encontrado.
	 */
	public String getTokenValue(final String key) {
		String token = getToken();
		if (StringUtils.isEmpty(token)) {
			throw new RuntimeException("Token Inválido.");
		}
		final Jwt jwtDecoder = JwtHelper.decode(token);
		Optional<Object> valueOfKey = Optional.empty();
		try {
			valueOfKey = valueOfKey(key, jwtDecoder);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return valueOfKey.isPresent() ? valueOfKey.get().toString() : null;

	}

	/**
	 * Método responsável por retornar o e-mail do usuário.
	 * 
	 * @return- E-mail.
	 */
	public String getEmailUsuario() {
		return getTokenValue(EMAIL);

	}

	public String getAuthorities() {
		return getTokenValue(AUTHORITIES);
	}

	public String getRealmAccess() {
		return getTokenValue(REALM_ACCESS);
	}

	public String getResourceAccess() {
		return getTokenValue(RESOURCE_ACCESS);
	}

	/**
	 * Método responsável por retornar o nome do usuário.
	 * 
	 * @return- Nome do usuário.
	 */
	public String getUsername() {
		String givenName = getTokenValue(GIVEN_NAME);
		if (StringUtils.isEmpty(givenName)) {
			givenName = getTokenValue(NAME);
		}
		return !StringUtils.isEmpty(givenName) ? givenName : getUserId();

	}

	/**
	 * Método responsável por retornar o nome do usuário.
	 * 
	 * @return- Nome do usuário.
	 */
	public String getUserId() {
		return getTokenValue(PREFERRED_USERNAME);

	}

	/**
	 * Método responsável por retornar o token gerado por oauth e openid.
	 * 
	 * @return Token
	 */
	public String getToken() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (null != authentication) {
			final OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) authentication.getDetails();
			if (null != details) {
				return details.getTokenValue();

			}
		}

		return null;
	}

	private static ObjectMapper getObjectMapper() {
		if (null == OBJECT_MAPPER) {
			OBJECT_MAPPER = new ObjectMapper();
		}
		return OBJECT_MAPPER;
	}

	private Optional<Object> valueOfKey(final String key, final Jwt jwtDecoder)
			throws IOException, JsonParseException, JsonMappingException {
		if (null != jwtDecoder && null != jwtDecoder.getClaims()) {
			final HashMap<String, Object> result = getMap(jwtDecoder);
			if (null != result && result.containsKey(key)) {
				return Optional.of(result.get(key));
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> getMap(final Jwt jwtDecoder)
			throws IOException, JsonParseException, JsonMappingException {
		return getObjectMapper().readValue(jwtDecoder.getClaims(), HashMap.class);
	}
}
