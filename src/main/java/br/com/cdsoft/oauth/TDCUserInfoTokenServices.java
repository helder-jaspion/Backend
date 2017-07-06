package br.com.cdsoft.oauth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

/***
 * Classe responsável por receber um token e retornar a autenticação baseada em
 * seus dados.
 * 
 * @author Cléber da Silveira.
 *         <p>
 *         Como nos serviços que não são UI estamos removendo o acesso ao
 *         servidor oauth para access_token e refresh_token o processo de
 *         expiration é manual.
 *         </p>
 *
 */
public class TDCUserInfoTokenServices extends UserInfoTokenServices {

	private static final String ROLES = "roles";
	private static final String REALM_ACCESS = "realm_access";
	private static final String EXPIRATION_DATE = "exp";
	private static final String CREDENTIALS = "N/A";
	private boolean ui;
	private AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();
	private String clientId;
	private static final Logger LOGGER = LoggerFactory.getLogger(TDCUserInfoTokenServices.class);
	private UserInfoTokenUtil userInfoTokenUtil;
	// 1970-01-01T0:0:0Z
	private static final int ZERO = 0;

	public TDCUserInfoTokenServices(final String userInfoEndpointUrl, final String clientId, final boolean isUI) {
		super(userInfoEndpointUrl, clientId);
		setAuthoritiesExtractor(authoritiesExtractor);
		this.clientId = clientId;
		this.ui = isUI;
	}

	public boolean isUI() {
		return ui;
	}

	@Override
	public OAuth2Authentication loadAuthentication(final String accessToken) {

		if (StringUtils.isEmpty(accessToken)) {
			throw new InvalidTokenException("Empty Token.");
		}

		if (isUI()) {
			return processAuthentication(accessToken, super.loadAuthentication(accessToken));
		} else {
			return extractAuthentication(getMap(JwtHelper.decode(accessToken)), accessToken);

		}
	}

	private OAuth2Authentication processAuthentication(final String accessToken, OAuth2Authentication oAuth2Authentication) {
		HashMap<String, Object> map = getMap(JwtHelper.decode(accessToken));
		final Object principal = getPrincipal(map);
		final List<GrantedAuthority> authorities = jwtAuthorities(getAuthoritiesExtractor().extractAuthorities(map),
				accessToken);
		return createAuthentication(map, principal, authorities);

	}

	private OAuth2Authentication createAuthentication(HashMap<String, Object> map, final Object principal,
			final List<GrantedAuthority> authorities) {
		final OAuth2Request request = new OAuth2Request(null, getClientId(), null, true, null, null, null, null, null);
		final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				principal, CREDENTIALS, authorities);
		usernamePasswordAuthenticationToken.setDetails(map);
		return new OAuth2Authentication(request, usernamePasswordAuthenticationToken);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<GrantedAuthority> jwtAuthorities(final List<GrantedAuthority> authorities, final String accessToken) {
		final HashMap<String, Object> map = getMap(JwtHelper.decode(accessToken));
		if (null != map) {
			final LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) map.get(REALM_ACCESS);
			if (null != hashMap) {
				final List roles = (List) hashMap.get(ROLES);
				if (null != roles && !roles.isEmpty()) {
					Collection<? extends GrantedAuthority> jwtAuthorities = (Collection<? extends GrantedAuthority>) roles
							.stream().map(mapper -> new SimpleGrantedAuthority(mapper.toString()))
							.collect(Collectors.toList());
					if (null != jwtAuthorities) {
						authorities.addAll(jwtAuthorities);
					}

				}
			}
		}

		return authorities;
	}

	public String getClientId() {
		return clientId;
	}

	public AuthoritiesExtractor getAuthoritiesExtractor() {
		return authoritiesExtractor;
	}

	public void setUserInfoTokenUtil(UserInfoTokenUtil userInfoTokenUtil) {
		this.userInfoTokenUtil = userInfoTokenUtil;
	}

	public UserInfoTokenUtil getUserInfoTokenUtil() {
		return userInfoTokenUtil;
	}

	private OAuth2Authentication extractAuthentication(final HashMap<String, Object> map, final String accessToken) {
		if (null == map) {
			throw new InvalidTokenException("Token not supported.");

		}
		expirationDate(map, accessToken);
		final Object principal = getPrincipal(map);
		final List<GrantedAuthority> authorities = getAuthoritiesExtractor().extractAuthorities(map);
		return createAuthentication(map, principal, authorities);

	}

	/**
	 * <b>exp</b> REQUIRED. Expiration time on or after which the ID Token MUST
	 * NOT be accepted for processing. The processing of this parameter requires
	 * that the current date/time MUST be before the expiration date/time listed
	 * in the value. Implementers MAY provide for some small leeway, usually no
	 * more than a few minutes, to account for clock skew. Its value is a JSON
	 * number representing the number of seconds from 1970-01-01T0:0:0Z as
	 * measured in UTC until the date/time. See RFC 3339 [RFC3339] for details
	 * regarding date/times in general and UTC in particular.
	 * 
	 * @param map
	 *            values of token.
	 * @param accessToken
	 *            token value
	 */
	private void expirationDate(final Map<String, Object> map, final String accessToken) {
		if (map.containsKey(EXPIRATION_DATE)) {
			Object expirationDate = map.get(EXPIRATION_DATE);
			if (null != expirationDate) {
				try {
					LocalDateTime expirationLocalDateTime = LocalDateTime.ofEpochSecond(ZERO, ZERO, ZoneOffset.UTC)
							.plus(Long.valueOf(expirationDate.toString()), ChronoUnit.SECONDS);
					if (expirationLocalDateTime.isBefore(LocalDateTime.now())) {
						throw new InvalidTokenException("Access token expired: " + accessToken);
					}
				} catch (NumberFormatException e) {
					LOGGER.error(e.getMessage(), e);
					throw new OAuth2Exception(e.getMessage(), e);
				}
			}
		}
	}

	private HashMap<String, Object> getMap(final Jwt jwtDecoder) {
		try {
			return getUserInfoTokenUtil().getMap(jwtDecoder);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new OAuth2Exception(e.getMessage(), e);
		}
	}

}
