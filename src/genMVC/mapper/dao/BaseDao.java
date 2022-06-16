package genMVC.mapper.dao;


import genMVC.Configuration;
import genMVC.utils.Utility;
import genMVC.model.Column;
import genMVC.model.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseDao<T> {
    static {
        loadProperties();
    }

    public static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();
    public static String url;
    public static String admin;
    public static String adminPassword;
    public static String driver;
    public static HashMap<String, Class<?>> modelTypeConvertMap;
    public Class<T> rawClass;
    public HashMap<String, Field> rawClassFieldMap;

    public BaseDao() {
        Type type = this.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // 参数化类型中可能有多个泛型参数
            Type[] types = parameterizedType.getActualTypeArguments();
            this.rawClass = (Class<T>) types[0];
        }
    }

    public static void loadProperties() {
        url = Configuration.url;
        admin = Configuration.admin;
        adminPassword = Configuration.adminPassword;
        driver = Configuration.driver;
        //
        modelTypeConvertMap = new HashMap<>();
        modelTypeConvertMap.put("Integer", Integer.class);
        modelTypeConvertMap.put("String", String.class);
        modelTypeConvertMap.put("Long", Long.class);
        modelTypeConvertMap.put("Date", Date.class);
        modelTypeConvertMap.put("Double", Double.class);
        modelTypeConvertMap.put("Time", Time.class);
        modelTypeConvertMap.put("Boolean", Boolean.class);
        modelTypeConvertMap.put("Float", Float.TYPE);
        modelTypeConvertMap.put("Timestamp", Timestamp.class);
        //
    }

    public static Connection connection() throws SQLException {
        Connection conn = null;
        try {
            conn = threadLocal.get();
            if (conn != null && !conn.isClosed()) {
                return conn;
            }
            Class.forName(driver);
            conn = DriverManager.getConnection(url, admin, adminPassword);
            if (conn != null && threadLocal.get() == null) {
                threadLocal.set(conn);
            }
        } catch (Exception e) {
            Utility.log("获取数据库连接异常 Cause by: %s", e.getMessage());
        }

        return conn;
    }

    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                threadLocal.set(null);
                threadLocal.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void beginTransaction(Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void commitTransaction(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Field findField(Object o) {
        Field[] declaredFields = this.rawClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (o.getClass() == field.getType()) {
                return field;
            }
        }
        return null;
    }

    // baseDao
    public <M> M factory(SQL sqlModel, Modify<M> Modify) {
        PreparedStatement prep = null;
        try {
            Connection conn = connection();

            String sql = sqlModel.content;
            if (sqlModel.generatedKey) {
                prep = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                prep = conn.prepareStatement(sql);
            }

            ArrayList<Object> parameters = sqlModel.parameters;
            if (parameters != null) {
                for (int i = 0; i < parameters.size(); i++) {
                    int index = i + 1;
                    Object ele = parameters.get(i);
                    Field field = this.findField(ele);
                    if (field != null && field.isAnnotationPresent(Column.class)) {
                        Column column = field.getAnnotation(Column.class);
                        String columnType = column.type();
                        if (columnType.trim().equals("")) {
                            prep.setObject(index, ele);
                        } else {
                            Method castor;
                            Object castedValue;
                            Class<?> type = this.modelTypeConvertMap.get(columnType);
                            if (columnType.equals("String")) {
                                castor = type.getDeclaredMethod("valueOf", Object.class);
                                castedValue = castor.invoke(type, ele);
                            } else if (columnType.equals("Date")) {
                                // ele 必须是 Long
                                castedValue = type.getDeclaredConstructor(Long.class).newInstance((Long) ele);
                            } else {
                                castor = type.getDeclaredMethod("valueOf", String.class);
                                castedValue = castor.invoke(type, ele.toString());
                            }
                            prep.setObject(index, castedValue);
                        }
                    } else {
                        prep.setObject(index, ele);
                    }
                }
            }

            M res = Modify.modify(prep);
            return res;
        } catch (Exception e) {
            Utility.log("数据库操作失败 Cause by: %s", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                prep.close();
            } catch (SQLException throwables) {
                Utility.log("prepareStatement 关闭失败 Cause by: %s", throwables.getMessage());
                throwables.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        List<Class<?>> classList = Configuration.classList;
        for (Class<?> c : classList) {
            if (c.isAnnotationPresent(Table.class)) {
                Field[] declaredFields = c.getDeclaredFields();
                for (Field f : declaredFields) {
                    if (f.isAnnotationPresent(Column.class)) {
                        Column annotation = f.getAnnotation(Column.class);
                        Utility.log("type: %s", annotation.type());
                        Utility.log("action: %s", annotation.action());
                        Utility.log("value: %s", annotation.value());
                    }
                }
            }
//            Utility.log("c: %s", c);
        }
    }

    public List<T> executeQuery(SQL SQLModel) {
        if (SQLModel == null) {
            return null;
        }
        return this.factory(SQLModel, (preparedStatement) -> {
            ResultSet res = preparedStatement.executeQuery();
            return modelsFromResult(res);
        });
    }

    public List<T> modelsFromResult(ResultSet res) throws Exception {
        List<T> list = new ArrayList<>();
        while (res.next()) {
            T obj = (T) this.rawClass.getDeclaredConstructor().newInstance();
            Field[] fields = this.rawClass.getDeclaredFields();
            String columnName = "";
            String type = "";
            for (Field field : fields) {
                field.setAccessible(true);

                boolean noted = field.isAnnotationPresent(Column.class);
                if (noted) {
                    Column annotation = field.getAnnotation(Column.class);
                    columnName = annotation.value();
                    type = annotation.type();
                } else {
                    columnName = field.getName();
                }

                Object o = res.getObject(columnName);
                if (type.equals("")) {
                    field.set(obj, o);
                } else {
                    Method castor;
                    Object castedValue;
                    String name = field.getType().getSimpleName();
                    if (name.equals("String")) {
                        castor = field.getType().getDeclaredMethod("valueOf", Object.class);
                        castedValue = castor.invoke(field.getType(), o);
                    } else {
                        castor = field.getType().getDeclaredMethod("valueOf", String.class);
                        castedValue = castor.invoke(field.getType(), o.toString());
                    }
                    field.set(obj, castedValue);
                }
            }
            list.add(obj);
        }

        return list;
    }

    public int executeUpdate(SQL SQLModel) {
        if (SQLModel == null) {
            return -1;
        }
        return this.factory(SQLModel, (preparedStatement) -> {
            int updatedColumns = preparedStatement.executeUpdate();
            return updatedColumns;
        });
    }

    public Object executeUpdateWithGeneratedKey(SQL SQLModel) {
        if (SQLModel == null) {
            return null;
        }
        return this.factory(SQLModel, (preparedStatement) -> {
            preparedStatement.executeUpdate();
            ResultSet res = preparedStatement.getGeneratedKeys();
            Object id = null;
            while (res.next()) {
                id = res.getObject(1);
            }
            return id;
        });
    }
}
