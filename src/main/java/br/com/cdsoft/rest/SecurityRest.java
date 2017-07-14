package br.com.cdsoft.rest;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.cdsoft.oauth.dto.JSONValue;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.OAuth2Definition;
import io.swagger.annotations.Scope;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;

@RestController
@SwaggerDefinition(

		basePath = "/mobile/sms/v1", host = "host", consumes = { "application/json", "application/xml", "" }, produces = {
				"application/json", "application/xml", "" }, schemes = { SwaggerDefinition.Scheme.HTTP,
						SwaggerDefinition.Scheme.HTTPS,
						SwaggerDefinition.Scheme.DEFAULT }, securityDefinition = @SecurityDefinition(oAuth2Definitions = {
								@OAuth2Definition(key = "oAuth2AccessCode", flow = OAuth2Definition.Flow.ACCESS_CODE, authorizationUrl = "http://oauth-tst.e-unicred.com.br/auth/", tokenUrl = "http://oauth-tst.e-unicred.com.br/auth/realms/UnicredRealm/protocol/openid-connect/token", scopes = {
										@Scope(name = "openid", description = "will only return the iss, sub, aud, exp, iat and at_hash claims"),
										@Scope(name = "profile", description = "will return the claims listed above, plus name, nickname, picture and updated_at"),
										@Scope(name = "email", description = " will return the claims listed above, plus email and email_verified")

								}), @OAuth2Definition(key = "oAuth2Password", flow = OAuth2Definition.Flow.PASSWORD) }), externalDocs = @ExternalDocs(value = "docs", url = "url_to_docs"))
@Api(value = "/resources", consumes = "application/json, application/xml", produces = "application/json, application/xml", protocols = "http", tags = {
		"api_tag1" }, authorizations = { @Authorization(value = "api_auth", scopes = {
				@AuthorizationScope(scope = "api_auth_scope", description = "api_auth_description") }) })
public class SecurityRest {
	@ApiOperation(value = "Retorna um recurso protegido", response = JSONValue.class)
	@ApiResponses({ @ApiResponse(code = 200, message = "Ok") })
	@Secured("ROLE_ADMIN")
	@RequestMapping({ "/protected/resource" })
	public JSONValue<String> getUserId() {
		return new JSONValue<String>("Protected");
	}

}
