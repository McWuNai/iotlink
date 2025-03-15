package com.yunze.common.utils.poi;

import java.util.List;

public class PageResult {
    private List<List<String>> rowData;
    private int pageNumber;
    private long totalRecords;

    // Constructors, getters and setters

    public PageResult(List<List<String>> rowData, int pageNumber, long totalRecords) {
        this.rowData = rowData;
        this.pageNumber = pageNumber;
        this.totalRecords = totalRecords;
    }

    public List<List<String>> getRowData() {
        return rowData;
    }

    public void setRowData(List<List<String>> rowData) {
        this.rowData = rowData;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }
}
