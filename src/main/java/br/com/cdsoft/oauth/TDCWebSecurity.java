package br.com.cdsoft.oauth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


/**
 * Contexto de configuração da segurança.
 * @author Cléber da Silveira.
 *
 */
@Configuration
@EnableWebSecurity
@EnableOAuth2Sso
@EnableResourceServer
public class TDCWebSecurity extends WebSecurityConfigurerAdapter {




	private static final String J_SPRING_SECURITY_CHECK = "/j_spring_security_check";

	private static final String SWAGGER = "/swagger**";

	private static final String REDIRECT_URI = "redirect_uri";

	private static final String JSESSIONID = "JSESSIONID";

	private static final String XSRF_TOKEN = "XSRF-TOKEN";

	private static final String KEYCLOAK_IDENTITY = "KEYCLOAK_IDENTITY";
	
	private static final String KC_RESTART = "KC_RESTART";
	
	private static final String KEYCLOAK_SESSION = "KEYCLOAK_SESSION";
	
	private static final String SESSION = "SESSION";
	
	

	private static final String LOGOUT = "/logout";
	
	private static final  String[] DEFAULT_PUBLIC_ACCESS = new String[] {"/public/**","/management/**", SWAGGER, "/webjars/**", "/swagger-resources/**", "/v2/api-docs/**", "/springfox-swagger/**", "/swagger-ui**"};
	@Autowired
	private ResourceServerProperties resourceServerProperties;

	@Autowired
	private OAuth2ProtectedResourceDetails oAuth2ProtectedResourceDetails;
	
	@Autowired
	private UserInfoTokenUtil userInfoTokenUtil;
	

	@Value("${keycloak.logout.endpoint:#{null}}")
	private String externalLogoutUrl;

	@Value("${keycloak.logout.redirect:#{null}}")
	private String redirect;
	
	@Value("${security.ignored:#{null}}")
	private String securityIgnored;
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		if (isUI()) {
			http.formLogin()
			.loginProcessingUrl(J_SPRING_SECURITY_CHECK)
			.and().authorizeRequests().anyRequest().authenticated().and().csrf()
					.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and().logout()
					.logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT)).invalidateHttpSession(true)
					.deleteCookies(XSRF_TOKEN, JSESSIONID, KEYCLOAK_IDENTITY, KC_RESTART, KEYCLOAK_SESSION, SESSION).clearAuthentication(true)
					.logoutSuccessUrl(externalLogoutUrl + "?" + REDIRECT_URI + "=" + redirect).and().headers()
					.frameOptions().sameOrigin();
		} else {
			http.antMatcher(SWAGGER).authorizeRequests().anyRequest().authenticated();
		}
		
	}

	private boolean isUI() {
		return !StringUtils.isEmpty(externalLogoutUrl) && !StringUtils.isEmpty(redirect);
	}

	@Override
	public void configure(final WebSecurity web) throws Exception {
		webConfigureAntMatchers(web);
	}
	/**
	 * Método responsável por retornar o serviço de informações do token.
	 * Não alterar o nome do método.
	 * @return {@link ResourceServerTokenServices}
	 */
	@Bean
	@Primary
	public ResourceServerTokenServices userInfoTokenServices() {
		final TDCUserInfoTokenServices unicredUserInfoTokenServices = new TDCUserInfoTokenServices(resourceServerProperties.getUserInfoUri(), resourceServerProperties.getClientId(), isUI());
		unicredUserInfoTokenServices.setTokenType(resourceServerProperties.getTokenType());
		unicredUserInfoTokenServices.setRestTemplate(new OAuth2RestTemplate(oAuth2ProtectedResourceDetails));
		unicredUserInfoTokenServices.setUserInfoTokenUtil(userInfoTokenUtil);
		return unicredUserInfoTokenServices;
	}
	private void webConfigureAntMatchers(final WebSecurity web) {
		final String[] ignoredSecurityPaths = getIgnoredSecurityPaths();
		if (null != ignoredSecurityPaths) {
			web.ignoring().antMatchers(ignoredSecurityPaths);
		}
	}

	private String[] getIgnoredSecurityPaths() {

		if (!StringUtils.isEmpty(securityIgnored) && securityIgnored.contains(",")) {
			return securityIgnored.split(",");
		}

		return DEFAULT_PUBLIC_ACCESS;
	}

}