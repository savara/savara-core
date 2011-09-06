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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author: Jeff Yu
 * @date: 19/04/11
 */

@Entity
@Inheritance
@DiscriminatorColumn(name="ACT_TYPE")
@Table(name="SAVARA_ACT")
public abstract class ActivityEntity {

    @Id
    @GeneratedValue
    @Column(name="ID")
    private long id;

    @Embedded
    private ComponentIDEntity componentId;

    @ManyToMany(mappedBy = "activities")
    private Collection<CorrelationIDEntity> correlationIds = new ArrayList<CorrelationIDEntity>();

    @Lob
    @Column(name="ACT_MODEL")
    private String activityModel;

    @Column(name="ACT_CONTEXT")
    private String properties;

    @Column(name="ACT_ID")
    private String actId;

    @Column(name="TIMESTAMP")
    private Date timestamp;

    @Column(name="DESC_CODE")
    private String descriptionCode;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getActivityModel() {
        return activityModel;
    }

    public void setActivityModel(String activityModel) {
        this.activityModel = activityModel;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getActId() {
        return actId;
    }

    public void setActId(String actId) {
        this.actId = actId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Collection<CorrelationIDEntity> getCorrelationIds() {
        return correlationIds;
    }

    public void setCorrelationIds(Collection<CorrelationIDEntity> correlationIds) {
        for (CorrelationIDEntity id : correlationIds) {
            id.getActivities().add(this);
        }
        this.correlationIds = correlationIds;
    }

    public ComponentIDEntity getComponentId() {
        return componentId;
    }

    public void setComponentId(ComponentIDEntity componentId) {
        this.componentId = componentId;
    }

    public String getDescriptionCode() {
        return descriptionCode;
    }

    public void setDescriptionCode(String descriptionCode) {
        this.descriptionCode = descriptionCode;
    }
}
