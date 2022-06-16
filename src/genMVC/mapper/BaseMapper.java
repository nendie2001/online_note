package genMVC.mapper;

import genMVC.utils.Utility;
import genMVC.mapper.dao.BaseDao;
import genMVC.mapper.dao.SQL;
import genMVC.model.Column;
import genMVC.model.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseMapper<T> extends BaseDao<T> {

    public String tableName;
    public String primaryKey;
    public String[] foreignKey;

    public String[] allFields;
    public HashMap<String, HashMap<String, String>> fieldMap = new HashMap<>();
    /*
    * QuestionId: <action: search>
    *
    * */

    public BaseMapper() {
        this.init();
    }

    public void init() {
        boolean notedTable = this.rawClass.isAnnotationPresent(Table.class);
        if (notedTable) {
            Table table = this.rawClass.getAnnotation(Table.class);
            this.tableName = table.name();
            this.primaryKey = table.primaryKey();
            this.foreignKey = table.foreignKey().split("\\|\\|");
            // field
            Field[] fs = this.rawClass.getDeclaredFields();
//            Utility.log("fs: %s", fs);
            this.allFields = new String[fs.length];
            for (int i = 0; i < fs.length; i++) {
                Field field = fs[i];
                this.allFields[i] = this.fieldName(field);
                // action
                if (field.isAnnotationPresent(Column.class)) {
                    Column c = field.getAnnotation(Column.class);
                    HashMap<String , String> m = new HashMap<>();
                    m.put("action", c.action());
                    fieldMap.put(this.fieldName(field), m);
                }
            }
            // 给 primary key 加上搜索
            HashMap<String , String> map = new HashMap<>();
            map.put("action", "search");
            fieldMap.put(this.primaryKey, map);
        } else {
            Utility.log("主类 缺失 @Table 注释");
        }
//        System.out.println(fieldMap.toString());
    }

    public String fieldName(Field field) {
        boolean notedColumn = field.isAnnotationPresent(Column.class);
        String v = "";
        if (notedColumn) {
            Column column = field.getAnnotation(Column.class);
            v = column.value();
        } else {
            v = field.getName();
        }
        return v;
    }

    public String placeHolder(String holderStr, String separator, Integer number) {
        String s = holderStr + separator;
        String res = "";
        for (int i = 0; i < number; i++) {
            res += s;
        }
        if (number == 1) {
            return res;
        }
        return res.substring(0, res.length() - separator.length());
    }

    public HashMap<String, Object> loadModel(T model) {
        Field[] fields = model.getClass().getFields();
        HashMap<String, Object> map = new HashMap<>();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object o = field.get(model);
                if (o == null) {
                    continue;
                }
//                String value;
//                if (o instanceof String) {
//                    String vs = String.format("'%s'", o);
//                    value = vs;
//                } else {
//                    value = String.valueOf(o);
//                }
                String key = this.fieldName(field);
                map.put(key, o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    public List<T> all() {
        String format = "select * from %s";
        String content = String.format(format, this.tableName);
        SQL sql = SQL.create(content);
        return this.executeQuery(sql);
    }

    public Object add(T model) {
        HashMap<String, Object> map = loadModel(model);
        String format = "insert into %s(%s) values (%s)";
        String c = String.join(", ", map.keySet());
        String valueHolder = placeHolder("?", ", ", map.size());
        String content = String.format(format, this.tableName, c, valueHolder);
        SQL sql = SQL.create(content, map.values());
        sql.generatedKey = true;
        return this.executeUpdateWithGeneratedKey(sql);
    }

    public void deleteById(Integer id) {
        String format = "delete from %s where %s = ?";
        String content = String.format(format, this.tableName, this.primaryKey);
        SQL sql = SQL.create(content, id);
        this.executeUpdate(sql);
    }

    public void updateById(T model) {
        HashMap<String, Object> map = this.loadModel(model);
        Integer id = (Integer) map.remove(this.primaryKey);
        StringBuilder kv = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String column = entry.getKey();
            String s = String.format("%s = ?", column);
            kv.append(this.placeHolder(s, ", ", 1));
        }
        String format = "update %s set %s where %s = ?";
        String content = String.format(format, this.tableName, kv.substring(0, kv.length() - 2), this.primaryKey);

        ArrayList<Object> values = new ArrayList<>();
        values.addAll(map.values());
        values.add(id);

        SQL sql = SQL.create(content, values);
        this.executeUpdate(sql);
    }

    public List<T> findByPrimaryKey(Object value) {
        return this.findByColumn(this.primaryKey, value);
    }

    public List<T> findByColumn(String columnName, Object value) {
        String format = "select * from %s where %s = ?";
        HashMap<String, String> actionMap = this.fieldMap.get(columnName);
        String a = actionMap.getOrDefault("action", "");
        String content = "";
        if (a.equals("search")) {
            content = String.format(format, this.tableName, columnName);
            SQL sql = SQL.create(content, value);
            return this.executeQuery(sql);
        } else {
            Utility.log("Column 无 Search 字段");
        }

        return null;
    }

    public List<T> findByColumnWithLike(String columnName, Object value) {
        String format = "select * from %s where %s like ?";
        HashMap<String, String> actionMap = this.fieldMap.get(columnName);
        String a = actionMap.get("action");

        String content = "";
        if (a.equals("search")) {
            content = String.format(format, this.tableName, columnName);
            SQL sql = SQL.create(content, "%" + value + "%");
            return this.executeQuery(sql);
        }

        return null;
    }
}
