package org.marsching.weave4j.dbo;

/**
 * Created by IntelliJ IDEA.
 * WeaveUser: termi
 * Date: 24.01.2010
 * Time: 15:43:15
 * To change this template use File | Settings | File Templates.
 */
public interface WeaveUserDAO {
    void createUser(String username, String password, String email);
    WeaveUser findUser(String username);
    void deleteUser(String username);
    void updatePassword(String username, String newPassword);
    void updateEMail(String username, String newEMail);
}
