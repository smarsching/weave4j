/*
 * weave4j - Weave Server for Java
 * Copyright (C) 2010  Sebastian Marsching
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

import java.util.HashSet;
import java.util.Set;

/**
 * Weave user.
 *
 * @author Sebastian Marsching
 */
public class WeaveUser {

    private String username;
    private String password;
    private String eMail;

    private Long artificialId;

    private Set<WeaveCollection> collections = new HashSet<WeaveCollection>();

    /**
     * Returns the artificial id. The artificial id is only used to identify the object within the database.
     *
     * @return artificial id
     */
    public Long getArtificialId() {
        return artificialId;
    }

    /**
     * Sets the artificial id. The artificial id should never be set by user code. It is automatically assigned
     * by Hibernate, when the object is stored within the database.
     *
     * @param artificialId artificial unique identifier
     */
    @SuppressWarnings("unused")
	private void setArtificialId(Long artificialId) {
        this.artificialId = artificialId;
    }

    /**
     * Returns the username of this user.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of this user. The username has to be unique.
     *
     * @param username username for this user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the (encrypted) password for this user.
     *
     * @return user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this user. The password passed to this function should be encrypted.
     *
     * @param password (encrypted) password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the e-mail address of the user.
     *
     * @return e-mail address
     */
    public String getEMail() {
        return eMail;
    }

    /**
     * Sets the e-mail address of the user.
     *
     * @param eMail e-mail address
     */
    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    /**
     * Returns the collections that exist for this user.
     *
     * @return collections for this user
     */
    public Set<WeaveCollection> getCollections() {
        return collections;
    }

    /**
     * Sets the collections for this user. This method should never be called by user code, because changes will
     * not be persisted to the database.
     *
     * @param collections collections for this user
     */
    public void setCollections(Set<WeaveCollection> collections) {
        this.collections = collections;
    }
}
