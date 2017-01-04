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
package com.dev.ops.common.audit;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.dev.ops.common.thread.local.ContextThreadLocal;
import com.dev.ops.common.utils.TimestampUtil;

/**
 * JPA listener to set audit column details whenever an entity is persisted.
 *
 * The listener interface for receiving auditColumns events. The class that is
 * interested in processing a auditColumns event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addAuditColumnsListener<code> method. When
 * the auditColumns event occurs, that object's appropriate
 * method is invoked.
 * @see AuditColumnsEvent
 */
public class AuditColumnsListener {

	@PrePersist
	void onCreate(final Object entity) {

		if(entity instanceof AuditableColumns) {

			final AuditableColumns auditableColumns = (AuditableColumns) entity;
			AuditColumns auditColumns = auditableColumns.getAuditColumns();

			if(auditColumns == null) {
				auditColumns = new AuditColumns();
				auditableColumns.setAuditColumns(auditColumns);
			}

			if(null == auditColumns.getCreatedById()) {
				auditColumns.setCreatedById(ContextThreadLocal.get().getTransactionRequestedByUserId());
			}

			if(null == auditColumns.getCreatedBy()) {
				auditColumns.setCreatedBy(ContextThreadLocal.get().getTransactionRequestedByUsername());
			}

			if(null == auditColumns.getCreatedDate()) {
				auditColumns.setCreatedDate(TimestampUtil.getCurentTimestamp());
			}
		}
	}

	@PreUpdate
	void onPersist(final Object entity) {

		if(entity instanceof AuditableColumns) {

			final AuditableColumns auditableColumns = (AuditableColumns) entity;

			AuditColumns auditColumns = auditableColumns.getAuditColumns();
			if(auditColumns == null) {
				auditColumns = new AuditColumns();
				auditableColumns.setAuditColumns(auditColumns);
			}

			auditColumns.setModifiedById(ContextThreadLocal.get().getTransactionRequestedByUserId());
			auditColumns.setModifiedBy(ContextThreadLocal.get().getTransactionRequestedByUsername());
			auditColumns.setModifiedDate(TimestampUtil.getCurentTimestamp());
		}
	}
}