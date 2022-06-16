package genMVC.http;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 包名:com.hjr.framework
 *
 * @author zyh
 * 日期2020-11-01  14:47
 */
public class BaseMethod implements Serializable {
    private Object object;
    private Method method; // add delete update

    public BaseMethod() {
    }

    public BaseMethod(Object object, Method method) {
        this.object = object;
        this.method = method;
    }

    @Override
    public String toString() {
        return "BaseMethod{" +
                "object=" + object +
                ", method=" + method +
                '}';
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
