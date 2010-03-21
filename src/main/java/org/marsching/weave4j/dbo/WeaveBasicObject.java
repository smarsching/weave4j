package org.marsching.weave4j.dbo;

import org.codehaus.jackson.annotate.*;

import java.math.BigDecimal;

/**
 * Weave Basic Object. This is the smallest entity stored by the Weave server.
 *
 * @author Sebastian Marsching
 */
@JsonAutoDetect(JsonMethod.NONE)
@JsonWriteNullProperties(false)
public class WeaveBasicObject {

    private Long artificialId;

    private String id;
    private String parentId;
    private String predecessorId;
    private BigDecimal modified;
    private Integer sortIndex;
    private String payload;

    private WeaveCollection collection;

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
     * Returns the WBO id. This id is unique for a within a collection.
     *
     * @return identifier identifying this WBO within its collection
     */
    @JsonGetter("id")
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier of this WBO. The identifier has to be unique within the
     * WBO's collection and may never be null.
     *
     * @param id identifier for this WBO
     */
    @JsonSetter("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier of the parent of this WBO or <code>null</code>, if no parent is set for this node.
     * The parent identifier can be used to arrange several WBOs in a tree like structure.
     *
     * @return parent node's identifier
     *
     */
    @JsonGetter("parentid")
    public String getParentId() {
        return parentId;
    }

    /**
     * Sets the parent node identifier. This identifier can be used to arrange WBOs in a tree like structure.
     *
     * @param parentId identifier of another WBO
     */
    @JsonSetter("parentid")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns the identifier of the predecessor of this WBO or <code>null</code>, if no predecessor is set for this
     * node. The predecessor identifier can be used to chain several WBOs in a list like structure.
     *
     * @return predecessor node's identifier
     *
     */
    @JsonGetter("predecessorid")
    public String getPredecessorId() {
        return predecessorId;
    }

    /**
     * Sets the predecessor node identifier. This identifier can be used to chain WBOs in a list like structure.
     *
     * @param predecessorId identifier of another WBO
     */
    @JsonSetter("predecessorid")
    public void setPredecessorId(String predecessorId) {
        this.predecessorId = predecessorId;
    }

    /**
     * Returns the time this WBO has been last modified in seconds since 01/01/1970.
     *
     * @return timestamp this WBO has been last modified
     */
    @JsonGetter("modified")
    public BigDecimal getModified() {
        return modified;
    }

    /**
     * Sets the timestamp this WBO has been last modified. Should be set each time a WBO is created or modified.
     *
     * @param modified timestamp of the last modification in seconds since 01/01/1970
     */
    @JsonSetter("modified")
    public void setModified(BigDecimal modified) {
        this.modified = modified;
    }

    /**
     * Returns the sort index of this WBO or <code>null</code> if no sort index is set. The sort index can be used
     * to define an order over several WBOs.
     *
     * @return sort index or <code>null</code>
     */
    @JsonGetter("sortindex")
    public Integer getSortIndex() {
        return sortIndex;
    }

    /**
     * Sets the sort index.
     *
     * @param sortIndex sort index
     */
    @JsonSetter("sortindex")
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    /**
     * Returns the payload of this WBO.
     *
     * @return payload
     */
    @JsonGetter("payload")
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the payload of the WBO. This can be an arbitrary string.
     *
     * @param payload arbitrary string representing the data stored in this WBO
     */
    @JsonSetter("payload")
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
     * @param collection collection this WBO should be assigned to
     */
    public void setCollection(WeaveCollection collection) {
        this.collection = collection;
    }
}
