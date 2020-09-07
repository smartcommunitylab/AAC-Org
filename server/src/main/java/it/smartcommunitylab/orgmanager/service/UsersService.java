package it.smartcommunitylab.orgmanager.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.dto.UserDTO;

@Service
public class UsersService {
    private final static Logger logger = LoggerFactory.getLogger(UsersService.class);

    @Autowired
    private ProfileService profileService;

    public UserDTO getUser(String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        logger.debug("get user by id " + userId);
        return UserDTO.from(profileService.getUserProfileById(userId));

    }

    public UserDTO getUserByUsername(String userName)
            throws IdentityProviderAPIException, NoSuchUserException {

        logger.debug("get user by username" + userName);
        return UserDTO.from(profileService.getUserProfile(userName));

    }

    public List<UserDTO> listUsers() throws IdentityProviderAPIException {

        logger.debug("list users");
        return profileService.searchUserProfiles("").stream().map(u -> UserDTO.from(u)).collect(Collectors.toList());
    }

    public List<UserDTO> getUsers(List<String> userIds) throws IdentityProviderAPIException {

        logger.debug("get  users for " + String.valueOf(userIds));
        return profileService.getUserProfiles(userIds).stream().map(u -> UserDTO.from(u)).collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String fullNameFilter) throws IdentityProviderAPIException {

        logger.debug("search users for " + String.valueOf(fullNameFilter));
        return profileService.searchUserProfiles(fullNameFilter).stream().map(u -> UserDTO.from(u))
                .collect(Collectors.toList());
    }

}
