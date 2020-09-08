package it.smartcommunitylab.orgmanager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AppController {

    @Value("${security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${security.oauth2.resourceserver.client-id}")
    private String clientId;

    @Value("${application.url}")
    private String applicationURL;

    @RequestMapping(method = RequestMethod.GET, value = "/env.js", produces = "text/javascript")
    public @ResponseBody String environment(Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("window.REACT_APP_OAUTH_ISSUER='").append(issuerUri).append("';");
        sb.append("window.REACT_APP_OAUTH_CLIENT_ID='").append(clientId).append("';");
        // sb.append("window.REACT_APP_API='").append(applicationURL+"/api").append("';");

        return sb.toString();
    }
}
