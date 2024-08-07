package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserChangePasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupListDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.GroupEntity;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserAlreadyExistsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByEmail(String email);

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto);

    /**
     * Register a new user.
     *
     * @param userLoginDto the user to register
     * @param admin        if the user is an admin
     * @return the JWT, if successful
     * @throws UserAlreadyExistsException if the user already exists
     */
    String register(UserRegisterDto userLoginDto, boolean admin) throws UserAlreadyExistsException;

    /**
     * Change the password of a user.
     *
     * @param changePasswordDto information of current and new password
     * @param username the username of the user
     */
    void changePassword(UserChangePasswordDto changePasswordDto, String username);

    /**
     * Getting the groups the user is part of.
     *
     * @param email email of the user
     * @return Set of groups the user is part of
     */
    Set<GroupEntity> getGroupsByUserEmail(String email);

    /**
     * Getting the recipes the user has created.
     *
     * @param email email of the user
     * @return List of recipes from the user
     */
    List<Recipe> getRecipesByUserEmail(String email);


    /**
     * Getting the groups the user is part of with debt information.
     *
     * @param email email of the user
     * @return Set of groups the user is part of with debt information
     */
    Set<GroupListDto> getGroupsByUserEmailWithDebtInfos(String email);
}
