package org.marsching.weave4j.dbo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * WeaveUser: termi
 * Date: 05.02.2010
 * Time: 02:05:16
 * To change this template use File | Settings | File Templates.
 */
public class WeaveCollection {
    private String name;

    private Long id;
    private WeaveUser user;
    private Set<WeaveBasicObject> weaveBasicObjects = new HashSet<WeaveBasicObject>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public WeaveUser getUser() {
        return user;
    }

    public void setUser(WeaveUser user) {
        this.user = user;
    }

    public Set<WeaveBasicObject> getWeaveBasicObjects() {
        return weaveBasicObjects;
    }

    public void setWeaveBasicObjects(Set<WeaveBasicObject> weaveBasicObjects) {
        this.weaveBasicObjects = weaveBasicObjects;
    }
}
