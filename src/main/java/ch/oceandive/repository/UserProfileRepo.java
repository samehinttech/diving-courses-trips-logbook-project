package ch.oceandive.repository;

import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;

/**
 * Service interface for handling user profile operations regardless of user type.
 * This provides a generic approach to handle different user types.
 */
@Repository
public interface UserProfileRepo {
    
    /**
     * Loads user profile data into the model based on username and user type.
     * 
     * @param username The username of the user
     * @param model The Spring MVC model to populate with user data
     * @return The user type as a string ("admin", "premium", etc.)
     * @throws Exception If the user profile cannot be loaded
     */
    String loadUserProfile(String username, Model model) throws Exception;
    
    /**
     * Loads user profile data for editing into the model based on username and user type.
     * 
     * @param username The username of the user
     * @param model The Spring MVC model to populate with user data
     * @return The user type as a string ("admin", "premium", etc.)
     * @throws Exception If the user profile cannot be loaded
     */
    String loadUserProfileForEdit(String username, Model model) throws Exception;
    
    /**
     * Updates a user profile with the provided data.
     * 
     * @param username The username of the user
     * @param user The user data object to update with
     * @return A success message
     * @throws Exception If the profile update fails
     */
    String updateUserProfile(String username, Object user) throws Exception;
}