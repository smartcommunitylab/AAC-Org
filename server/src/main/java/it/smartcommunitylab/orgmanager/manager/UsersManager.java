package it.smartcommunitylab.orgmanager.manager;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.dto.UserDTO;
import it.smartcommunitylab.orgmanager.service.UsersService;

@Service
public class UsersManager {
    private final static Logger logger = LoggerFactory.getLogger(UsersManager.class);

    @Autowired
    private UsersService userService;

    public UserDTO getUser(String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin only - TODO fix
        if (!OrgManagerUtils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return userService.getUser(userId);

    }

    public UserDTO getUserByUsername(String userName)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin only - TODO fix
        if (!OrgManagerUtils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return userService.getUserByUsername(userName);
    }

    public List<UserDTO> listUsers() throws IdentityProviderAPIException {

        // Admin only - TODO fix
        if (!OrgManagerUtils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return userService.listUsers();
    }

    public List<UserDTO> getUsers(List<String> userIds) throws IdentityProviderAPIException {

        // Admin only - TODO fix
        if (!OrgManagerUtils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return userService.getUsers(userIds);
    }

    public List<UserDTO> searchUsers(String fullNameFilter) throws IdentityProviderAPIException {

        // Admin only - TODO fix
        if (!OrgManagerUtils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return userService.searchUsers(fullNameFilter);
    }

}
