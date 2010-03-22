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

import java.math.BigDecimal;
import java.util.List;

/**
 * Provides methods to access WBOs and collections in the database.
 *
 * @author Sebastian Marsching
 */
public interface WeaveStorageDAO {

    /**
     * Order in which WBOs should be returned from a query.
     */
    public enum SortOrder {
        OLDEST, NEWEST, INDEX
    }

    /**
     * Returns the timestamp a collection has been last modified.
     * This is the the timestamp of the latest modification of any WBO stored within the collection.
     * If no WBO is stored within the collection, <code>null</code> is returned.
     *
     * @param user Weave user
     * @param collection type of the collection
     * @return the timestamp of the last modification of this WBO or <code>null</code>
     */
    BigDecimal getLastModified(WeaveUser user, String collection);

    /**
     * Returns the number of WBOs stored within a collection.
     *
     * @param user Weave user
     * @param collection type of the collection
     * @return number of WBOs stored within the collection
     */
    int getWBOCount(WeaveUser user, String collection);

    /**
     * Returns the WBO identified by the specified parameters or <code>null</code> if none such WBO is found.
     *
     * @param user Weave user
     * @param collection type of the collection
     * @param id identifier of the WBO
     * @return the Weave Basic Object identified by the parameters
     */
    WeaveBasicObject getWBO(WeaveUser user, String collection, String id);

    /**
     * Retuns all WBOs from a collection that satisfy the specified predicates.
     * If <code>null</code> is specified for a predicate, it is not used to restrict the result set.
     *
     * @param user Weave user (mandatory)
     * @param collection type of the collection (mandatory)
     * @param ids return WBOs that match this list of WBO identifiers (optional)
     * @param predecessorId return WBOs that have this  predecessor id (optional)
     * @param parentId return WBOs that have this parent id (optional)
     * @param modifiedBefore return WBOs that have been modified before the given timestamp (optional)
     * @param modifiedSince return WBOs that have been modified since the given timestamp (optional)
     * @param sortIndexAbove return WBOs that have a sort index above the given value (optional)
     * @param sortIndexBelow return WBOs that have a sort index below the given value (optional)
     * @param limit limit the number of WBOs returned (optional)
     * @param offset skip the number of WBOs returned (from the beginning of the list) (optional)
     * @param sortOrder order in which the returned WBOs are sorted (optional)
     * @return list of WBOs matching the predicates
     */
    List<WeaveBasicObject> getWBOsFromCollection(
            WeaveUser user,
            String collection,
            List<String> ids,
            String predecessorId,
            String parentId,
            BigDecimal modifiedBefore,
            BigDecimal modifiedSince,
            Integer sortIndexAbove,
            Integer sortIndexBelow,
            Integer limit,
            Integer offset,
            SortOrder sortOrder);

    /**
     * Stores a WBO in the database. The collection type specified here will override the collection specified within
     * the WBO.
     *
     * @param user Weave user
     * @param collection collection type
     * @param wbo the Weave Basic Object to be stored
     */
    void insertWBO(WeaveUser user, String collection, WeaveBasicObject wbo);

    /**
     * Deletes a WBO frm the database.
     *
     * @param wbo the WBO which shall be deleted
     */
    void deleteWBO(WeaveBasicObject wbo);

    /**
     * Deletes a collection and all WBOs stored within this collection.
     *
     * @param user Weave user
     * @param collection type of the collection
     */
    void deleteCollection(WeaveUser user, String collection);

    /**
     * Deletes all collections and all WBOs of a user.
     *
     * @param user Weave user
     */
    void deleteAllCollections(WeaveUser user);
}
