package com.tpp.persistence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SearchResult<T> implements Serializable {

	private static final long serialVersionUID = -7902513195700602072L;

	public SearchResult() {
		this(null, 0, 0, 0, 0, false, false);
	}

	public SearchResult(List<T> list, long totalRows, int pageSize, int numOfPages, int pageIndex,
			boolean hasPreviousPage, boolean hasNextPage) {
		this.list = (list == null ? new ArrayList<T>() : list);
		this.totalRows = totalRows;
		this.pageSize = pageSize;
		this.numOfPages = numOfPages;
		this.pageIndex = pageIndex;
		this.hasPreviousPage = hasPreviousPage;
		this.hasNextPage = hasNextPage;
	}

	private long totalRows;
	private int pageSize;
	private int numOfPages;
	private int pageIndex;
	private boolean hasPreviousPage;
	private boolean hasNextPage;
	private Meta meta = new Meta();

	private List<T> list;

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public long getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(long totalRows) {
		this.totalRows = totalRows;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getNumOfPages() {
		return numOfPages;
	}

	public void setNumOfPages(int numOfPages) {
		this.numOfPages = numOfPages;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public boolean isHasPreviousPage() {
		return hasPreviousPage;
	}

	public void setHasPreviousPage(boolean hasPreviousPage) {
		this.hasPreviousPage = hasPreviousPage;
	}

	public boolean isHasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public void buildPagingParams(int pageIndex, int pageSize, long rowCount) {
		int numOfPages = (int) (Math.ceil(((double) rowCount) / ((double) pageSize)));
		this.setTotalRows(rowCount);
		this.setPageSize(pageSize);
		this.setNumOfPages(numOfPages);
		this.setPageIndex(pageIndex);
		this.setHasPreviousPage(pageIndex > 1);
		this.setHasNextPage(pageIndex < numOfPages);
	}

}
