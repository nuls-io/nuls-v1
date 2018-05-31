package io.nuls.core.tools.page;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {

    private int pageNumber;

    private int pageSize;

    private long total;

    private int pages;

    private List<T> list;

    public Page() {
        this.list = new ArrayList<>();
    }

    public Page(int pageNumber, int pageSize) {
        this();
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public Page(int pageNumber, int pageSize, int total) {
        this(pageNumber, pageSize);
        this.total = total;

        int p = total / pageSize;
        if (total % pageSize > 0) {
            p++;
        }
        this.pages = p;
    }

    public Page(Page page) {
        this.pageNumber = page.getPageNumber();
        this.pageSize = page.getPageSize();
        this.total = page.getTotal();
        this.pages = page.getPages();
        this.list = new ArrayList<>();
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
