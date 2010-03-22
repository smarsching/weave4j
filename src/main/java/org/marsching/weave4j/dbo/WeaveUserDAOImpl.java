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
