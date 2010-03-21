package org.marsching.weave4j.dbo;

import java.util.HashSet;
import java.util.Set;

/**
 * Weave collection. A collection aggregates several basic objects. Each collection is assigned
 * to a user. There is always only one collection of a given type per user.
 *
 * @author Sebastian Marsching
 */
public class WeaveCollection {
    private Long artificialId;

    private String type;
    private WeaveUser user;
    
    private Set<WeaveBasicObject> weaveBasicObjects = new HashSet<WeaveBasicObject>();

    /**
     * Returns the type of this collection. The type identifies the type of the WBOs that are stored in this collection.
     *
     * @return type of this collection
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this collection.
     *
     * @param type type of this collection
     */
    public void setType(String type) {
        this.type = type;
    }

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
     * Returns the user this collection is assigned to.
     *
     * @return user this collection is assigned to
     */
    public WeaveUser getUser() {
        return user;
    }

    /**
     * Sets the user this collection is assigned to.
     *
     * @param user user this collection shall be assigned to
     */
    public void setUser(WeaveUser user) {
        this.user = user;
    }

    /**
     * Returns all WBOs that are stored within this collection.
     *
     * @return WBOs stored within this collection
     */
    public Set<WeaveBasicObject> getWeaveBasicObjects() {
        return weaveBasicObjects;
    }

    /**
     * Sets the WBOs stored in this collection. This method should not be used by user code, because changes to
     * the collection will not be persisted to the database.
     *
     * @param weaveBasicObjects WBOs stored in this collection
     */
    public void setWeaveBasicObjects(Set<WeaveBasicObject> weaveBasicObjects) {
        this.weaveBasicObjects = weaveBasicObjects;
    }
}
