package Dot.Mapper;

import Dot.Model.BeanModel;
import genMVC.mapper.BaseMapper;
import genMVC.mapper.dao.SQL;

import java.util.List;

public class BeanMapper extends BaseMapper<BeanModel> {
    public List<BeanModel> all(Integer currentPage, Integer size, Integer userId) {
        String content = String.format("select * from %s where deleted = 'false' and userId = ? order by updatedTime desc limit ?, ?", this.tableName);
        Integer begin = (currentPage - 1) * size;
        if (begin < 0) {
            begin = 0;
        }
        Integer offset = size;
        SQL sql = SQL.create(content, userId, begin, offset);
        return this.executeQuery(sql);
    }

    public List<BeanModel> all(Integer currentPage, Integer size, Integer userId, String beanContent) {
        String content = String.format("select * from %s where content like ? and deleted = 'false' and userId = ? order by updatedTime desc limit ?, ?", this.tableName);
        Integer begin = (currentPage - 1) * size;
        if (begin < 0) {
            begin = 0;
        }
        Integer offset = size;
        String bc = "%" + beanContent + "%";
        SQL sql = SQL.create(content, bc, userId, begin, offset);
        return this.executeQuery(sql);
    }

    public List<BeanModel> findByBeanId(Integer beanId) {
        String content = String.format("select * from %s where deleted = 'false' and id = ?", this.tableName);
        SQL sql = SQL.create(content, beanId);
        return this.executeQuery(sql);
    }

    public List<BeanModel> findByUserId(Integer userId) {
        String content = String.format("select * from %s where deleted = 'false' and userId = ? order by updatedTime desc", this.tableName);
        SQL sql = SQL.create(content, userId);
        return this.executeQuery(sql);
    }

    public List<BeanModel> findByUserIdAndContent(Integer id, String c) {
        String content = String.format("select * from %s where content like ? and deleted = 'false' and userId = ? order by updatedTime desc", this.tableName);
        SQL sql = SQL.create(content, "%" + c + "%", id);
        return this.executeQuery(sql);
    }
}
