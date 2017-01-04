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