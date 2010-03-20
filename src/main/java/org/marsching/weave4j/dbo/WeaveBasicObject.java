package org.marsching.weave4j.dbo;

import org.codehaus.jackson.annotate.*;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * WeaveUser: termi
 * Date: 05.02.2010
 * Time: 01:44:21
 * To change this template use File | Settings | File Templates.
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

    public Long getArtificialId() {
        return artificialId;
    }

    public void setArtificialId(Long artificialId) {
        this.artificialId = artificialId;
    }

    @JsonGetter("id")
    public String getId() {
        return id;
    }

    @JsonSetter("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonGetter("parentid")
    public String getParentId() {
        return parentId;
    }

    @JsonSetter("parentid")
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @JsonGetter("predecessorid")
    public String getPredecessorId() {
        return predecessorId;
    }

    @JsonSetter("predecessorid")
    public void setPredecessorId(String predecessorId) {
        this.predecessorId = predecessorId;
    }

    @JsonGetter("modified")
    public BigDecimal getModified() {
        return modified;
    }

    @JsonSetter("modified")
    public void setModified(BigDecimal modified) {
        this.modified = modified;
    }

    @JsonGetter("sortindex")
    public Integer getSortIndex() {
        return sortIndex;
    }

    @JsonSetter("sortindex")
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    @JsonGetter("payload")
    public String getPayload() {
        return payload;
    }

    @JsonSetter("payload")
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public WeaveCollection getCollection() {
        return collection;
    }

    public void setCollection(WeaveCollection collection) {
        this.collection = collection;
    }
}
