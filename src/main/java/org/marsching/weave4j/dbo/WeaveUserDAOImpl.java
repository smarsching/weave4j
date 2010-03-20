package org.marsching.weave4j.dbo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.marsching.weave4j.dbo.exception.InvalidUserException;

/**
 * Implementation of {@link org.marsching.weave4j.dbo.WeaveUserDAO}.
 *
 * @author Sebastian Marsching
 */
public class WeaveUserDAOImpl implements WeaveUserDAO {
    private SessionFactory sessionFactory;

    public void createUser(String username, String password, String eMail) {
        Session session = sessionFactory.getCurrentSession();
        WeaveUser user = new WeaveUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEMail(eMail);
        session.save(user);
        session.flush();
    }

    public WeaveUser findUser(String username) {
        return (WeaveUser) sessionFactory.getCurrentSession().createQuery("from WeaveUser user where user.username = ?").setString(0, username).uniqueResult();
    }

    public void deleteUser(String username) {
        Session session = sessionFactory.getCurrentSession();
        WeaveUser user = findUser(username);
        if (user == null) {
            throw new InvalidUserException("WeaveUser \"" + username + "\" not found");
        }
        session.delete(user);
        session.flush();
    }

    public void updatePassword(String username, String newPassword) {
        WeaveUser user = findUser(username);
        if (user == null) {
            throw new InvalidUserException("WeaveUser \"" + username + "\" not found");
        }
        user.setPassword(newPassword);
        sessionFactory.getCurrentSession().update(user);
    }

    public void updateEMail(String username, String newEMail) {
        WeaveUser user = findUser(username);
        if (user == null) {
            throw new InvalidUserException("WeaveUser \"" + username + "\" not found");
        }
        user.setEMail(newEMail);
        sessionFactory.getCurrentSession().update(user);
    }

    /**
     * Sets the Hibernate session factory used by this DAO.
     *
     * @param sessionFactory Hibernate session factory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    } 
}
