package com.dev.ops.common.dao.generic;

import java.util.Collection;

import com.dev.ops.common.domain.ContextInfo;

public interface GenericDAO<T> {

	T save(T object, ContextInfo contextInfo);

	T save(T object);

	T update(T object, ContextInfo contextInfo);

	T update(T object);

	Collection<T> update(Collection<T> object, ContextInfo contextInfo);

	Collection<T> update(Collection<T> object);

	void delete(Object id, ContextInfo contextInfo);

	void delete(Object id);

	T findByPrimaryKey(Object id, ContextInfo contextInfo);

	T findByPrimaryKey(Object id);
}