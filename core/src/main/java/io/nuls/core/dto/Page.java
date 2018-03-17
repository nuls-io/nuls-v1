package io.nuls.core.dto;

import java.util.List;

public class Page<T> {

    private int pageNumber;

    private int pageSize;

    private long total;

    private int pages;

    private List<T> list;

    public Page() {

    }

    public Page(Page page) {
        this.pageNumber = page.getPageNumber();
        this.pageSize = page.getPageSize();
        this.total = page.getTotal();
        this.pages = page.getPages();
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (pageSize != 0) {
            this.pages = (int) (total / pageSize);
            if (total % pageSize != 0) {
                this.pages++;
            }
        }
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
