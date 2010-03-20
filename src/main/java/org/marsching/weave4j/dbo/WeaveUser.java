package org.marsching.weave4j.dbo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * WeaveUser: termi
 * Date: 24.01.2010
 * Time: 13:41:56
 * To change this template use File | Settings | File Templates.
 */
public class WeaveUser {

    private String username;
    private String password;
    private String eMail;

    private Long id;
    private Set<WeaveCollection> collections = new HashSet<WeaveCollection>();

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public Set<WeaveCollection> getCollections() {
        return collections;
    }

    public void setCollections(Set<WeaveCollection> collections) {
        this.collections = collections;
    }
}
