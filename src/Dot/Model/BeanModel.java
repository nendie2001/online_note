package Dot.Model;

import genMVC.model.BaseModel;
import genMVC.model.Column;
import genMVC.model.Table;

@Table(name = "bean", primaryKey = "id")
public class BeanModel extends BaseModel {
    @Column(value = "id", action = "search")
    public Integer id;
    public String selfId;
    public String content;
    @Column(value = "userId", action = "search")
    public Integer userId;
    @Column(value = "color", type = "String")
    public BeanColor color;
    public Long createdTime;
    public Long updatedTime;
    @Column(value = "deleted", type = "String")
    public Boolean deleted;
    // Long unixTime = System.currentTimeMillis() / 1000L;


    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getSelfId() {
        return selfId;
    }

    public void setSelfId(String selfId) {
        this.selfId = selfId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public BeanColor getColor() {
        return color;
    }

    public void setColor(BeanColor color) {
        this.color = color;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
