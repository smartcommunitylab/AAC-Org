package it.smartcommunitylab.orgmanager.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.UserDTO;
import it.smartcommunitylab.orgmanager.manager.UsersManager;

@RestController
@Validated
public class UsersController {

    @Autowired
    private UsersManager userManager;

    /*
     * Global
     */

    @GetMapping("/api/users")
    public List<UserDTO> searchUsers(
            @RequestParam(required = false, name = "id") String[] ids,
            @RequestParam(required = false, name = "q") String nameFilter)
            throws SystemException, IdentityProviderAPIException {

        if (StringUtils.hasText(nameFilter)) {
            return userManager.searchUsers(nameFilter);
        } else if (!ArrayUtils.isEmpty(ids)) {
            return userManager.getUsers(Arrays.asList(ids));
        } else {
            return userManager.listUsers();
        }

    }

    @GetMapping("/api/users/{userId}")
    public UserDTO getUser(
            @PathVariable @Valid @Pattern(regexp = Constants.USERID_PATTERN) String userId)
            throws SystemException, IdentityProviderAPIException, NoSuchUserException {
        return userManager.getUser(userId);
    }

}
