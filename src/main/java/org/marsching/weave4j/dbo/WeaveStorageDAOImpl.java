package org.marsching.weave4j.dbo;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: termi
 * Date: 14.03.2010
 * Time: 12:11:42
 * To change this template use File | Settings | File Templates.
 */
public class WeaveStorageDAOImpl implements WeaveStorageDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public BigDecimal getLastModified(WeaveUser user, String collection) {
        BigDecimal lastModified = (BigDecimal) sessionFactory.getCurrentSession().createQuery("select max(wbo.modified) from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.name = ? and wbo.collection = c").setEntity(0, user).setString(1, collection).uniqueResult();
        if (lastModified == null) {
            return new BigDecimal(0);
        } else {
            return lastModified;
        }
    }

    public int getWBOCount(WeaveUser user, String collection) {
        Integer count = (Integer) sessionFactory.getCurrentSession().createQuery("select count(wbo) from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.name = ? and wbo.collection = c").setEntity(0, user).setString(1, collection).uniqueResult();
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    public WeaveBasicObject getWBO(WeaveUser user, String collection, String id) {
        return (WeaveBasicObject) sessionFactory.getCurrentSession().createQuery("select wbo from WeaveBasicObject wbo, WeaveCollection c where c.user = ? and c.name = ? and wbo.collection = c and wbo.id = ?").setEntity(0, user).setString(1, collection).setString(2, id).uniqueResult();
    }

    public List<WeaveBasicObject> getWBOsFromCollection(WeaveUser user, String collection, List<String> ids, String predecessorId, String parentId, BigDecimal modifiedBefore, BigDecimal modifiedSince, Integer sortIndexAbove, Integer sortIndexBelow, Integer limit, Integer offset, SortOrder sortOrder) {
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(WeaveBasicObject.class);
        criteria.createCriteria("collection")
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("name", collection));
        Disjunction idDisjunction = Restrictions.disjunction();
        for (String id : ids) {
            idDisjunction.add(Restrictions.eq("id", id));
        }
        criteria.add(idDisjunction);
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
        WeaveCollection weaveCollection = (WeaveCollection) session.createQuery("select c from WeaveCollection c where c.user = ? and c.name = ?").setEntity(0, user).setString(1, collection).uniqueResult();
        if (weaveCollection == null) {
            weaveCollection = new WeaveCollection();
            weaveCollection.setUser(user);
            weaveCollection.setName(collection);
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
        WeaveCollection weaveCollection = (WeaveCollection) session.createQuery("select c from WeaveCollection c where c.user = ? and c.name = ?").setEntity(0, user).setString(1, collection).uniqueResult();
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
}
