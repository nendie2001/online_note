package Dot.Model;

import genMVC.model.BaseModel;

import java.util.List;

public class Page<T> extends BaseModel {
    // 当前页码
    private Integer pageNo;
    // 总页码
    private Integer pageTotal;
    // 当前页显示数量
    private Integer pageSize;
    // 总记录数
    private Integer pageTotalCount;
    // 当前页数据
    private List<T> items;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        //上边界判断
        if (pageNo <= 1) {
            pageNo = 1;
        }
        //下边界判断, 在业务层调用时,一定要先有pageTotal(总页数)
        if (pageNo >= pageTotal) {
            pageNo = pageTotal;
        }
        if (this.pageTotal == 0) {
            pageNo = 0;
        }
        this.pageNo = pageNo;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageTotalCount() {
        return pageTotalCount;
    }

    public void setPageTotalCount(Integer pageTotalCount) {
        this.pageTotalCount = pageTotalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
