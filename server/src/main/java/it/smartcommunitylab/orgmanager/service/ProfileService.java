package it.smartcommunitylab.orgmanager.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.aac.AACException;
import it.smartcommunitylab.aac.AACProfileService;
import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.aac.model.BasicProfiles;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.config.SecurityConfig;

@Service
@Transactional
public class ProfileService {
    private final static Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private AACProfileService aacProfileService;

    @Autowired
    private SecurityConfig securityConfig;

    /**
     * Initializes the service to obtain client tokens.
     */
    @PostConstruct
    private void init() {
        // Generates the service to obtain the proper client tokens needed for certain
        // calls to the identity provider's APIs
        aacProfileService = securityConfig.getAACProfileService();
    }

    /**
     * Obtains a BasicProfile from the identity provider, representing the profile
     * of the user with the input user name.
     * 
     * @param userName - User name to query the identity provider with
     * @return - Profile of the requested user
     * @throws IdentityProviderAPIException
     * @throws NoSuchUserException
     */
    public BasicProfile getUserProfile(String userName) throws IdentityProviderAPIException, NoSuchUserException {
        if (userName == null || userName.equals("")) {
            // invalid request
            return null;
        }
        logger.debug("get profile for username " + userName);
        try {
            BasicProfiles profiles = aacProfileService.searchUsersByUsername(getToken(), userName);
            if (profiles == null || profiles.getProfiles() == null || profiles.getProfiles().isEmpty()) {
                throw new NoSuchUserException(
                        "Profile for user " + userName + " could not be found; unable to continue.");
            }
            return profiles.getProfiles().get(0);
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to obtain profile information: " + e.getMessage());
        }
    }

    /**
     * Obtains a BasicProfile from the identity provider, representing the profile
     * of the user with the input user name.
     * 
     * @param userName - User name to query the identity provider with
     * @return - Profile of the requested user
     * @throws IdentityProviderAPIException
     * @throws NoSuchUserException
     */
    public BasicProfile getUserProfileById(String userId) throws IdentityProviderAPIException, NoSuchUserException {
        if (userId == null || userId.equals("")) {
            // invalid request
            return null;
        }

        logger.debug("get profile for userId " + String.valueOf(userId));
        try {
            BasicProfile profile = aacProfileService.getUser(getToken(), userId);
            if (profile == null) {
                throw new NoSuchUserException(
                        "Profile for user " + userId + " could not be found; unable to continue.");
            }
            return profile;
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to obtain profile information: " + e.getMessage());
        }
    }

    /**
     * Generates a client access token with the input scope.
     * 
     * @param scope - Scope the token needs to have
     * @return - Access token with the desired scope
     * @throws IdentityProviderAPIException
     */
    private String getToken() throws IdentityProviderAPIException {
        return securityConfig.getToken(Constants.SCOPE_MANAGE_ROLES);
    }
}
