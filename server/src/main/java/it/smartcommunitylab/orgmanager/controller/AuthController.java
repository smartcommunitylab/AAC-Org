package it.smartcommunitylab.orgmanager.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.service.ProfileService;

@Controller
@Api(value = "/auth")
public class AuthController {

    private final static Logger _log = LoggerFactory.getLogger(AuthController.class);

    @Value("${security.oauth2.client.user-authorization-uri}")
    private String authorizationURL;

    @Value("${security.oauth2.client.access-token-uri}")
    private String tokenURL;

    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    @Value("${security.oauth2.client.scopes}")
    private String oauthScopes;

    @Value("${application.url}")
    private String applicationURL;

    @Value("${client.url}")
    private String clientURL;

    @Value("${aac.uri}")
    private String aacUri;

    @Autowired
    private ProfileService profileService;

    /*
     * Login
     */

    @GetMapping(value = "/api/auth/login", produces = "application/json")
    @ApiOperation(value = "Login users via OAuth")
    public RedirectView login(RedirectAttributes attributes,
            HttpServletRequest request, HttpServletResponse response) {

        String currentURL = request.getRequestURL().toString();
        String callbackURL = currentURL.replace("/auth/login", "/auth/callback");

        if (!applicationURL.isEmpty()) {
            callbackURL = applicationURL.concat("/api/auth/callback");
        }

        // build authorization parameters
        attributes.addAttribute("response_type", "code");
        attributes.addAttribute("client_id", clientId);
        attributes.addAttribute("scope", oauthScopes);
        attributes.addAttribute("redirect_uri", callbackURL);
        // TODO create and store state var

        _log.debug("send redirect to oauth at " + authorizationURL);
        _log.debug(attributes.asMap().toString());

        return new RedirectView(authorizationURL, false);
    }

    @GetMapping(value = "/api/auth/callback", produces = "application/json")
    @ApiOperation(value = "Login callback via OAuth")
    public void callback(RedirectAttributes attributes,
            HttpServletRequest request, HttpServletResponse response) throws LoginException {

        String currentURL = request.getRequestURL().toString();
        String redirectURI = currentURL;
//        String redirectURL = "/#/callback";
        String redirectURL = currentURL.replace("/api/auth/callback", "/");

        if (!applicationURL.isEmpty()) {
            redirectURI = applicationURL.concat("/api/auth/callback");
            redirectURL = applicationURL.concat("/");
        }

        if (!clientURL.isEmpty()) {
            redirectURL = clientURL.concat("/");
        }

        String code = request.getParameter("code");
        String state = request.getParameter("state");

        if (code == null || code.isEmpty()) {
            throw new LoginException("invalid code");
        }

        _log.debug("oauth callback");
        _log.trace("oauth authorization code " + code);

        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // buid auth header as basic
        String auth = clientId + ":" + clientSecret;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("UTF-8")));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        // build token parameters
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
//        map.add("client_id", clientId);
//        map.add("client_secret", clientSecret);
        map.add("redirect_uri", redirectURI);

        _log.trace("call token url at " + tokenURL);
        _log.trace(map.toString());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        ResponseEntity<String> result = template.exchange(tokenURL, HttpMethod.POST, entity, String.class);
        if (result.getStatusCode() != HttpStatus.OK) {
            _log.error("token response code " + String.valueOf(result.getStatusCodeValue()));
            _log.debug(result.getBody());
            throw new LoginException("invalid token response");
        }

        try {
            // parse as json
            JSONObject json = new JSONObject(result.getBody());

            _log.trace(json.toString());

            // extract tokens
            String accessToken = json.optString("access_token", "");
            String refreshToken = json.optString("refresh_token", "");
            String expiresIn = json.optString("expires_in", "");

            _log.trace("access token " + accessToken);
            _log.trace("refresh token " + refreshToken);

            if (accessToken.isEmpty()) {
                _log.error("empty access token");
                throw new LoginException("empty access token");
            }

            // fetch userInfo
            // TODO if needed

            // append token - should be already urlencoded
            redirectURL = redirectURL.concat("#access_token=" + accessToken + "&expires_in=" + expiresIn);

            _log.debug("send redirect to " + redirectURL);
            response.sendRedirect(redirectURL);

        } catch (JSONException jex) {
            _log.error("json parsing error " + jex.getMessage());
            throw new LoginException("response error");
        } catch (IOException iex) {
            _log.error("io error " + iex.getMessage());
            throw new LoginException("network error");
        }
    }

    @GetMapping(value = "/api/auth/profile", produces = "application/json")
    @ApiOperation(value = "Get user profile")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, allowEmptyValue = false, paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
    })
    @ResponseBody
    public BasicProfile userProfile(
            HttpServletRequest request, HttpServletResponse response)
            throws LoginException, IdentityProviderAPIException, NoSuchUserException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new LoginException();
        }

        _log.error("user name "+auth.getName());
        BasicProfile profile =  profileService.getUserProfileById(auth.getName());
        _log.error(profile.toString());
        return profile;
    }

    @ExceptionHandler(LoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String loginError(LoginException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NoSuchUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String notFound(NoSuchUserException ex) {
        return ex.getMessage();
    }
}