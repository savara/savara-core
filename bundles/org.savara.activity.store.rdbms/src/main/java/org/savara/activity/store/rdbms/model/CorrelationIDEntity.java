/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.savara.activity.store.rdbms.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Author: Jeff Yu
 * Date: 19/04/11
 */

@Entity
@Table(name = "SAVARA_ACT_CORRELATION_ID")
public class CorrelationIDEntity implements Serializable{

    @Id
    @GeneratedValue
    @Column(name="ID")
    private long id;

    @Column(name="VALUE")
    private String value;

    @ManyToOne
    private CorrelationIDEntity parent;

    @OneToMany(cascade = CascadeType.PERSIST)
    private Collection<CorrelationIDEntity> children = new ArrayList<CorrelationIDEntity>();

    @ManyToMany
    private Collection<ActivityEntity> activities = new ArrayList<ActivityEntity>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CorrelationIDEntity getParent() {
        return parent;
    }

    public void setParent(CorrelationIDEntity parent) {
        this.parent = parent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Collection<ActivityEntity> getActivities() {
        return activities;
    }

    public void setActivities(Collection<ActivityEntity> activities) {
        this.activities = activities;
    }

    public void addChild(CorrelationIDEntity child) {
        this.children.add(child);
        child.setParent(this);
    }

    public Collection<CorrelationIDEntity> getChildren() {
        return children;
    }

    public void setChildren(Collection<CorrelationIDEntity> children) {
        this.children = children;
    }
}
