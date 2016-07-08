package com.dev.ops.common.audit;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Audit columns to maintain created/modified details of the entity.
 * Each non-lookup table has audit columns. These columns would be embedded within relevant persistent entity.
 */
@Embeddable
public class AuditColumns {

	@Column(name = "created_by_id")
	private String createdById;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_date")
	private Timestamp createdDate;

	@Column(name = "modified_by_id")
	private String modifiedById;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "modified_date")
	private Timestamp modifiedDate;

	public String getCreatedById() {
		return this.createdById;
	}

	public void setCreatedById(final String createdById) {
		this.createdById = createdById;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(final Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedById() {
		return this.modifiedById;
	}

	public void setModifiedById(final String modifiedById) {
		this.modifiedById = modifiedById;
	}

	public String getModifiedBy() {
		return this.modifiedBy;
	}

	public void setModifiedBy(final String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Timestamp getModifiedDate() {
		return this.modifiedDate;
	}

	public void setModifiedDate(final Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Override
	public String toString() {
		return "AuditColumns [createdById=" + this.createdById + ", createdBy=" + this.createdBy + ", createdDate=" + this.createdDate + ", modifiedById=" + this.modifiedById + ", modifiedBy=" + this.modifiedBy + ", modifiedDate=" + this.modifiedDate + "]";
	}

}