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

/**
 * Generic data access object 
 * @author rick@robinsonhq.com
 * @param <T>
 */
public interface GenericDAO<T> {

    /**
     * Save data 
     * @param object to persist
     * @return saved object 
     */
    T save(T object);

    /**
     * Update data
     * @param object to update 
     * @return updated object
     */
    T update(T object);

    /**
     * Delete object 
     * @param id of the object to delete
     */
    void delete(Object id);

    /**
     * Find data by primary key
     * @param id of the data to be returned
     * @return data 
     */
    T findByPrimaryKey(Object id);
}
