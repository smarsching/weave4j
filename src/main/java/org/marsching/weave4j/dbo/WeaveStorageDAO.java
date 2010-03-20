package org.marsching.weave4j.dbo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: termi
 * Date: 06.02.2010
 * Time: 01:07:05
 * To change this template use File | Settings | File Templates.
 */
public interface WeaveStorageDAO {
    public enum SortOrder {
        OLDEST, NEWEST, INDEX
    }

    BigDecimal getLastModified(WeaveUser user, String collection);
    int getWBOCount(WeaveUser user, String collection);
    WeaveBasicObject getWBO(WeaveUser user, String collection, String id);
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
    void insertWBO(WeaveUser user, String collection, WeaveBasicObject wbo);
    void deleteWBO(WeaveBasicObject wbo);
    void deleteCollection(WeaveUser user, String collection);
    void deleteAllCollections(WeaveUser user);
}
