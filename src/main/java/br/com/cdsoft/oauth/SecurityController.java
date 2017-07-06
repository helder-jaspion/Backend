package br.com.cdsoft.oauth;

import java.security.Principal;
import java.util.Collection;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cdsoft.oauth.dto.JSONValue;


/**
 * Controlador Rest para informações do usuário.
 * 
 * @author Cléber da Silveira.
 *
 */
@RestController
public class SecurityController {

	private @Autowired UserInfoTokenUtil userInfoTokenUtil;

	@RequestMapping({ "/user/name" })
	public JSONValue<String> getUserName(final Principal principal) {
		return new JSONValue<String>(userInfoTokenUtil.getUsername());
	}

	@RequestMapping({ "/user/id" })
	public JSONValue<String> getUserId() {
		return new JSONValue<String>(userInfoTokenUtil.getUserId());
	}

	@RequestMapping({ "/user/authorities" })
	public Collection<? extends GrantedAuthority> getUserAuthorities() {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities();
	}

	@RequestMapping({ "/user/roles" })
	public JSONValue<String> getRealmAccess() {
		return new JSONValue<String>(userInfoTokenUtil.getRealmAccess());
	}

	@RequestMapping({ "/user/resources" })
	public JSONValue<String> getResourceAccess() {
		return new JSONValue<String>(userInfoTokenUtil.getResourceAccess());
	}

	@RequestMapping({ "/user/mail" })
	public JSONValue<String> getUserMail() {
		return new JSONValue<String>(userInfoTokenUtil.getEmailUsuario());
	}

	@RequestMapping({"/user/token"})
	@Produces(value={MediaType.APPLICATION_JSON})
	public JSONValue<String> getToken() {
		String token = userInfoTokenUtil.getToken();
		return new JSONValue<String>(token);
	}

	@RequestMapping(value = { "/user", "/me" })
	public Principal user(Principal user) {
		return user;
	}
}
