package com.tpp.persistence;

import java.io.Serializable;
import java.util.Date;

/**
 */
public abstract class AbstractEntity<ID extends Serializable> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3831883043692825896L;

	private ID id;
	private Date createdAt;
	private Date updatedAt;
	private String createdBy;
	private String updatedBy;

	public ID getId() {
		return id;
	}

	public void setId(ID id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
