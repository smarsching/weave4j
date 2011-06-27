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
import java.math.BigInteger;

/**
 * Weave Basic Object. This is the smallest entity stored by the Weave server.
 * 
 * @author Sebastian Marsching
 */
public class WeaveBasicObject {

    private Long artificialId;

    private String id;
    private String parentId;
    private String predecessorId;
    private BigDecimal modified;
    private BigInteger ttl;
    private Integer sortIndex;
    private String payload;

    private WeaveCollection collection;

    /**
     * Returns the artificial id. The artificial id is only used to identify the
     * object within the database.
     * 
     * @return artificial id
     */
    public Long getArtificialId() {
        return artificialId;
    }

    /**
     * Sets the artificial id. The artificial id should never be set by user
     * code. It is automatically assigned by Hibernate, when the object is
     * stored within the database.
     * 
     * @param artificialId
     *            artificial unique identifier
     */
    @SuppressWarnings("unused")
    private void setArtificialId(Long artificialId) {
        this.artificialId = artificialId;
    }

    /**
     * Returns the WBO id. This id is unique for a within a collection.
     * 
     * @return identifier identifying this WBO within its collection
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of this WBO. The identifier has to be unique within
     * the WBO's collection and may never be null.
     * 
     * @param id
     *            identifier for this WBO
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier of the parent of this WBO or <code>null</code>, if
     * no parent is set for this node. The parent identifier can be used to
     * arrange several WBOs in a tree like structure.
     * 
     * @return parent node's identifier
     * 
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parent node identifier. This identifier can be used to arrange
     * WBOs in a tree like structure.
     * 
     * @param parentId
     *            identifier of another WBO
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns the identifier of the predecessor of this WBO or
     * <code>null</code>, if no predecessor is set for this node. The
     * predecessor identifier can be used to chain several WBOs in a list like
     * structure.
     * 
     * @return predecessor node's identifier
     * 
     */
    public String getPredecessorId() {
        return predecessorId;
    }

    /**
     * Sets the predecessor node identifier. This identifier can be used to
     * chain WBOs in a list like structure.
     * 
     * @param predecessorId
     *            identifier of another WBO
     */
    public void setPredecessorId(String predecessorId) {
        this.predecessorId = predecessorId;
    }

    /**
     * Returns the time this WBO has been last modified in seconds since
     * 01/01/1970.
     * 
     * @return timestamp this WBO has been last modified
     */
    public BigDecimal getModified() {
        return modified;
    }

    /**
     * Sets the timestamp this WBO has been last modified. Should be set each
     * time a WBO is created or modified.
     * 
     * @param modified
     *            timestamp of the last modification in seconds since 01/01/1970
     */
    public void setModified(BigDecimal modified) {
        this.modified = modified;
    }

    /**
     * Returns the time to live (TTL). The TTL is the point in time after which
     * this WBO will be automatically deleted. This means, that the WBO will
     * only be available to the client before this point in time. The time has
     * the form of a UNIX time-stamp (number of seconds since 01/01/1970). If
     * the TTL is <code>null</code>, this WBO will exist forever, until it is
     * explicitly deleted by the client.
     * 
     * @return time to live (TTL)
     */
    public BigInteger getTtl() {
        return ttl;
    }

    /**
     * Sets the time to live (TTL). The TTL is the point in time after which
     * this WBO will be automatically deleted. This means, that the WBO will
     * only be available to the client before this point in time. The time has
     * the form of a UNIX time-stamp (number of seconds since 01/01/1970).
     * 
     * @param ttl
     *            time to live (TTL)
     */
    public void setTtl(BigInteger ttl) {
        this.ttl = ttl;
    }

    /**
     * Returns the sort index of this WBO or <code>null</code> if no sort index
     * is set. The sort index can be used to define an order over several WBOs.
     * 
     * @return sort index or <code>null</code>
     */
    public Integer getSortIndex() {
        return sortIndex;
    }

    /**
     * Sets the sort index.
     * 
     * @param sortIndex
     *            sort index
     */
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    /**
     * Returns the payload of this WBO.
     * 
     * @return payload
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the payload of the WBO. This can be an arbitrary string.
     * 
     * @param payload
     *            arbitrary string representing the data stored in this WBO
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    /**
     * Returns the collection this WBO is part of.
     * 
     * @return collection this WBO is part of
     */
    public WeaveCollection getCollection() {
        return collection;
    }

    /**
     * Sets the collection this WBO is part of.
     * 
     * @param collection
     *            collection this WBO should be assigned to
     */
    public void setCollection(WeaveCollection collection) {
        this.collection = collection;
    }
}
