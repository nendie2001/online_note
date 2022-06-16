package Dot.Mapper;

import Dot.Model.UserModel;
import genMVC.mapper.BaseMapper;
import genMVC.mapper.dao.SQL;

import java.util.List;

public class UserMapper extends BaseMapper<UserModel> {
    public List<UserModel> all() {
        String format = "select * from %s";
        String content = String.format(format, this.tableName);
        SQL sql = SQL.create(content);
        return this.executeQuery(sql);
    }

    public UserModel findById(Integer id) {
        List<UserModel> u = this.findByColumn("id", id);
        if (u.size() == 1) {
            return u.get(0);
        }
        return null;
    }

    public List<UserModel> findByUsernameAndPassword(UserModel userModel) {
        String content = String.format("select * from %s where username = ? and password = ?", super.tableName);
        SQL sql = SQL.create(content, userModel.username, userModel.password);
        List<UserModel> res = this.executeQuery(sql);
        return res;
    }

    public List<UserModel> findByUsername(String username) {
        return this.findByColumn("username", username);
    }
}
