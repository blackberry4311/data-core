package com.tpp.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class SearchCriteria<T> implements Serializable {

	private static final long serialVersionUID = -7501057241800803697L;

	public static final int MAX_PAGE_SIZE = Integer.valueOf(System.getProperty("sql.query.maxPageSize", "1000"));

	// search properties
	private T criteria;
	private List<JoinCriteria> joinCriteria = new ArrayList<>();
	private Meta meta = new Meta();
	CustomFieldFilter customFieldFilter;

	// sort properties
	private String sortName = null;
	private boolean isSortAsc = true;

	//sorts property
	private LinkedHashSet<OrderBy> ordersBy = new LinkedHashSet<>();

	// page properties
	private int pageIndex = 1;
	private int pageSize = Integer.valueOf(System.getProperty("sql.query.defaultPageSize", "100"));

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public boolean isSortAsc() {
		return isSortAsc;
	}

	public void setSortAsc(boolean sortAsc) {
		this.isSortAsc = sortAsc;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public T getCriteria() {
		return criteria;
	}

	public void setCriteria(T criteria) {
		this.criteria = criteria;
	}

	public List<JoinCriteria> getJoinCriteria() {
		return joinCriteria;
	}

	public void setJoinCriteria(List<JoinCriteria> joinCriteria) {
		this.joinCriteria = joinCriteria;
	}

	public Meta getMeta() {
		return meta;
	}

	public CustomFieldFilter getCustomFieldFilter() {
		return customFieldFilter;
	}

	public void setCustomFieldFilter(CustomFieldFilter customFieldFilter) {
		this.customFieldFilter = customFieldFilter;
	}

	public void setMeta(Meta props) {
		if (props != null) meta.putAll(props);
		else meta.clear();
	}

	public LinkedHashSet<OrderBy> getOrdersBy() {
		return ordersBy;
	}

	public void setOrdersBy(LinkedHashSet<OrderBy> ordersBy) {
		this.ordersBy = ordersBy;
	}

	@Override
	public String toString() {
		return "SearchCriteria{" + "pageIndex=" + pageIndex + ", pageSize=" + pageSize + ", sortName='" + sortName
				+ '\'' + ", isSortAsc=" + isSortAsc + ", criteria=" + criteria + ", meta=" + meta + '}';
	}

	public static class OrderBy {
		private String sortName;
		private boolean isSortAsc = true;

		public String getSortName() {
			return sortName;
		}

		public void setSortName(String sortName) {
			this.sortName = sortName;
		}

		public boolean isSortAsc() {
			return isSortAsc;
		}

		public void isSortAsc(boolean isSortAsc) {
			this.isSortAsc = isSortAsc;
		}
	}
}
