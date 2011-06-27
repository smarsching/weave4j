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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Implementation of {@link org.marsching.weave4j.dbo.WeaveStorageDAO}.
 *
 * @author Sebastian Marsching
 */
public class WeaveStorageDAOImpl implements WeaveStorageDAO {

    private SessionFactory sessionFactory;

    /**
     * Sets the Hibernate session factory used by this DAO.
     *
     * @param sessionFactory Hibernate session factory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public BigDecimal getLastModified(WeaveUser user, String collection, BigInteger timestamp) {
        BigDecimal lastModified = (BigDecimal) sessionFactory.getCurrentSession().createQuery("select max(wbo.modified) from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.type = ? and wbo.collection = c and (wbo.ttl is null or wbo.ttl >= ?)").setEntity(0, user).setString(1, collection).setBigInteger(2, timestamp).uniqueResult();
        if (lastModified == null) {
            return new BigDecimal(0);
        } else {
            return lastModified;
        }
    }

    public int getWBOCount(WeaveUser user, String collection, BigInteger timestamp) {
        Integer count = (Integer) sessionFactory.getCurrentSession().createQuery("select count(wbo) from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.type = ? and wbo.collection = c and (wbo.ttl is null or wbo.ttl >= ?)").setEntity(0, user).setString(1, collection).setBigInteger(2, timestamp).uniqueResult();
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    public long getCollectionSize(WeaveUser user, String collection,
            BigInteger timestamp) {
        Number size = (Number) sessionFactory.getCurrentSession().createQuery("select sum(length(wbo.payload)) from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.type = ? and wbo.collection = c and (wbo.ttl is null or wbo.ttl >= ?)").setEntity(0, user).setString(1, collection).setBigInteger(2, timestamp).uniqueResult();
        return size.longValue() / 1024L;
    }

    public long getTotalSize(WeaveUser user, BigInteger timestamp) {
        Number size = (Number) sessionFactory.getCurrentSession().createQuery("select sum(length(wbo.payload)) from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and wbo.collection = c and (wbo.ttl is null or wbo.ttl >= ?)").setEntity(0, user).setBigInteger(1, timestamp).uniqueResult();
        return size.longValue() / 1024L;
    }

    public WeaveBasicObject getWBO(WeaveUser user, String collection, String id, BigInteger timestamp) {
        return (WeaveBasicObject) sessionFactory.getCurrentSession().createQuery("select wbo from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.type = ? and wbo.collection = c and wbo.id = ? and (wbo.ttl is null or wbo.ttl >= ?)").setEntity(0, user).setString(1, collection).setString(2, id).setBigInteger(3, timestamp).uniqueResult();
    }

    public List<WeaveBasicObject> getWBOsFromCollection(WeaveUser user, String collection, List<String> ids, String predecessorId, String parentId, BigDecimal modifiedBefore, BigDecimal modifiedSince, Integer sortIndexAbove, Integer sortIndexBelow, Integer limit, Integer offset, SortOrder sortOrder, BigInteger timestamp) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(WeaveBasicObject.class)
            .add(Restrictions.or(Restrictions.isNull("ttl"), Restrictions.ge("ttl", timestamp)));
        criteria.createCriteria("collection")
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("type", collection));
        if (ids != null) {
            Disjunction idDisjunction = Restrictions.disjunction();
            for (String id : ids) {
                idDisjunction.add(Restrictions.eq("id", id));
            }
            criteria.add(idDisjunction);
        }
        if (predecessorId != null) {
            criteria.add(Restrictions.eq("predecessorId", predecessorId));
        }
        if (parentId != null) {
            criteria.add(Restrictions.eq("parentId", parentId));
        }
        if (modifiedBefore != null) {
            criteria.add(Restrictions.lt("modified", modifiedBefore));
        }
        if (modifiedSince != null) {
            criteria.add(Restrictions.ge("modified", modifiedSince));
        }
        if (sortIndexAbove != null) {
            criteria.add(Restrictions.gt("sortIndex", sortIndexAbove));
        }
        if (sortIndexBelow != null) {
            criteria.add(Restrictions.lt("sortIndex", sortIndexBelow));
        }
        if (limit != null) {
            criteria.setMaxResults(limit);
        }
        if (offset != null) {
            criteria.setFirstResult(offset);
        }
        if (sortOrder != null) {
            switch (sortOrder) {
                case OLDEST:
                    criteria.addOrder(Order.asc("modified"));
                    break;
                case NEWEST:
                    criteria.addOrder(Order.desc("modified"));
                    break;
                case INDEX:
                    criteria.addOrder(Order.desc("sortIndex"));
                    break;
                default:
                    throw new IllegalArgumentException("SortOrder " + sortOrder + " is not supported");
            }
        }
        @SuppressWarnings("unchecked")
        List<WeaveBasicObject> wbos = criteria.list();
        return wbos;
    }

    public void insertWBO(WeaveUser user, String collection, WeaveBasicObject wbo) {
        Session session = sessionFactory.getCurrentSession();
        WeaveCollection weaveCollection = (WeaveCollection) session.createQuery("select c from WeaveCollection c where c.user = ? and c.type = ?").setEntity(0, user).setString(1, collection).uniqueResult();
        if (weaveCollection == null) {
            weaveCollection = new WeaveCollection();
            weaveCollection.setUser(user);
            weaveCollection.setType(collection);
            session.save(weaveCollection);
        }
        wbo.setCollection(weaveCollection);
        session.save(wbo);
        session.flush();
    }

    public void deleteWBO(WeaveBasicObject wbo) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(wbo);
        session.flush();
    }

    public void deleteCollection(WeaveUser user, String collection) {
        Session session = sessionFactory.getCurrentSession();
        WeaveCollection weaveCollection = (WeaveCollection) session.createQuery("select c from WeaveCollection c where c.user = ? and c.type = ?").setEntity(0, user).setString(1, collection).uniqueResult();
        deleteCollection(weaveCollection);
    }

    private void deleteCollection(WeaveCollection weaveCollection) {
        Session session = sessionFactory.getCurrentSession();
        if (weaveCollection == null) {
            return;
        }
        for (WeaveBasicObject wbo : weaveCollection.getWeaveBasicObjects()) {
            session.delete(wbo);
        }
        session.delete(weaveCollection);
        session.flush();
    }

    public void deleteAllCollections(WeaveUser user) {
        for (WeaveCollection weaveCollection : user.getCollections()) {
            deleteCollection(weaveCollection);
        }
    }

    public void cleanUpExpiredWBOs(BigInteger timestamp) {
        sessionFactory.getCurrentSession().createQuery("delete WeaveBasicObject wbo where wbo.ttl is not null and wbo.ttl < ?").setBigInteger(0, timestamp).executeUpdate();
    }
}
