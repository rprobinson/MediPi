/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.dev.ops.common.dao.generic;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.utils.LoggingUtil;
import com.dev.ops.exception.manager.constants.LoggingConstants.LoggingPoint;

@Service
public abstract class GenericDAOImpl<T> implements GenericDAO<T> {

	private static final Logger LOGGER = LogManager.getLogger(GenericDAOImpl.class);

	private EntityManager entityManager;

	private final Class<T> type;

	@SuppressWarnings({"rawtypes", "unchecked"})
	public GenericDAOImpl() {
		final Type t = this.getClass().getGenericSuperclass();
		final ParameterizedType pt = (ParameterizedType) t;
		this.type = (Class) pt.getActualTypeArguments()[0];
	}

	@PersistenceContext
	public void setEntityManager(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	@Override
	public T save(final T object, final ContextInfo contextInfo) {
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {"[save]"}, contextInfo));
		this.getEntityManager().persist(object);
		LOGGER.debug("Object persisted:" + object + " of type:" + object.getClass() + (null != contextInfo ? contextInfo : ""));
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {"[save]"}, contextInfo));
		return object;
	}

	@Override
	public T save(final T object) {
		return this.save(object, null);
	}

	@Override
	public T update(final T object, final ContextInfo contextInfo) {
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {"[update]"}, contextInfo));
		final T updatedObject = this.getEntityManager().merge(object);
		LOGGER.debug("Object updated:" + object + " of type:" + object.getClass() + (null != contextInfo ? contextInfo : ""));
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {"[update]"}, contextInfo));
		return updatedObject;
	}

	@Override
	public T update(final T object) {
		return this.update(object, null);
	}

	@Override
	public Collection<T> update(final Collection<T> objects, final ContextInfo contextInfo) {
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {"[bulk update]"}, contextInfo));
		Collection<T> updatedObjects = null;

		if(objects instanceof List) {
			updatedObjects = new ArrayList<T>();
		} else {
			updatedObjects = new HashSet<T>();
		}

		for(final T object : objects) {
			updatedObjects.add(this.getEntityManager().merge(object));
		}
		LOGGER.debug("Objects bulk updated:" + updatedObjects + " of type:" + (null != contextInfo ? contextInfo : ""));
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {"[bulk update]"}, contextInfo));
		return updatedObjects;
	}

	@Override
	public Collection<T> update(final Collection<T> objects) {
		return this.update(objects, null);
	}

	@Override
	public void delete(final Object id, final ContextInfo contextInfo) {
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {"[delete]"}, contextInfo));
		this.getEntityManager().remove(this.getEntityManager().getReference(this.type, id));
		LOGGER.debug("Object Deleted:<" + id + ">" + (null != contextInfo ? contextInfo : ""));
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {"[delete]"}, contextInfo));
	}

	@Override
	public void delete(final Object id) {
		this.delete(id, null);
	}

	@Override
	public T findByPrimaryKey(final Object id, final ContextInfo contextInfo) {
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.START.toString(), new Object[] {"[findByPrimaryKey]"}, contextInfo));
		final T object = this.getEntityManager().find(this.type, id);
		LOGGER.debug("Find entity by primary key:<" + id + ">" + (null != contextInfo ? contextInfo : ""));
		LOGGER.debug(LoggingUtil.getMessageDescription(LoggingPoint.END.toString(), new Object[] {"[findByPrimaryKey]"}, contextInfo));
		return object;
	}

	@Override
	public T findByPrimaryKey(final Object id) {
		return this.findByPrimaryKey(id, null);
	}
}