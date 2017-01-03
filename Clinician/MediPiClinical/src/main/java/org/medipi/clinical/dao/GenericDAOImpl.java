/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.clinical.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.medipi.clinical.logging.MediPiLogger;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Repository;

/**
 * Implementation of Generic Data Access Object
 * @author rick@robinsonhq.com
 * @param <T> Object to be persisted/deleted/updated etc
 */
@Repository
public abstract class GenericDAOImpl<T> implements GenericDAO<T> {

    @Autowired
    private MediPiLogger logger;

    private EntityManager entityManager;

    private final Class<T> type;

    /**
     * Constructor
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public GenericDAOImpl() {
        final Type t = this.getClass().getGenericSuperclass();
        final ParameterizedType pt = (ParameterizedType) t;
        this.type = (Class) pt.getActualTypeArguments()[0];
    }

    /**
     * Setter for entityManager
     * @param entityManager
     */
    @PersistenceContext
    public void setEntityManager(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Getter for entityManager
     * @return
     */
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public T save(final T object) {
        this.getEntityManager().persist(object);
        logger.log(object.getClass().getName() + ".info", "Object persisted:" + object + " of type:" + object.getClass());
        return object;
    }

    @Override
    public T update(final T object) {
        final T updatedObject = this.getEntityManager().merge(object);
        logger.log(object.getClass().getName() + ".info", "Object updated:" + object + " of type:" + object.getClass());
        return updatedObject;
    }

    @Override
    public void delete(final Object id) {
        this.getEntityManager().remove(this.getEntityManager().getReference(this.type, id));
        logger.log(id.getClass().getName() + ".info", "Object Deleted:<" + id + ">");
    }

    @Override
    public T findByPrimaryKey(final Object id) {
        final T object = this.getEntityManager().find(this.type, id);
        logger.log(id.getClass().getName() + ".info", "Find entity by primary key:<" + id + ">");
        return object;
    }
}
