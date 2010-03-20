package org.marsching.weave4j.dbo;

/**
 * Provides methods to access the users stored in the database.
 *
 * @author Sebastian Marsching
 */
public interface WeaveUserDAO {

    /**
     * Creates a user and stores it in the database.
     *
     * @param username username of the user
     * @param password encrypted password of the user
     * @param email e-mail address of the user
     */
    void createUser(String username, String password, String email);

    /**
     * Returns a user from the database.
     *
     * @param username username of the user
     * @return Weave user or <code>null</code> if no user is found for the given username
     */
    WeaveUser findUser(String username);

    /**
     * Deletes a user from the database.
     *
     * @param username username of the user to be deleted
     */
    void deleteUser(String username);

    /**
     * Changes the password of a user.
     *
     * @param username username of the user
     * @param newPassword encrypted password to set for the user
     */
    void updatePassword(String username, String newPassword);

    /**
     * Changes the e-mail address of a user.
     *
     * @param username username of the user
     * @param newEMail e-mail address to set for the user
     */
    void updateEMail(String username, String newEMail);
}
