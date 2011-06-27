/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2010-2011  Sebastian Marsching
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marsching.weave4j.dbo;

import java.util.Collection;

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
     * Returns all users in the database.
     * 
     * @return all users in the database
     */
    Collection<? extends WeaveUser> getUsers();

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
