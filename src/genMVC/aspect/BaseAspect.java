package genMVC.aspect;

import genMVC.Configuration;
import genMVC.utils.Utility;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BaseAspect implements MethodInterceptor {
    static {
        loadAllAspect();
    }

    public interface Point {
        Object proceed(BaseAspect baseAspect, Method method, Object[] objects) throws Throwable;
    }

    private static List<Point> list;
    private int index;
    private Object target;

    public BaseAspect() {

    }

    public static void loadAllAspect() {
        list = new ArrayList<>();
        List<Class<?>> as = Configuration.classList;
        try {
            // 默认事务 AOP
            Point transactionAspect = TransactionAspect.class.getDeclaredConstructor().newInstance();
            list.add(transactionAspect);
            // 用户自定义
            for (Class a : as) {
                if (a.isAnnotationPresent(Aspect.class)) {
                    Point aspectInstance = (Point) a.getDeclaredConstructor().newInstance();
                    list.add(aspectInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Utility.log("all Aspect: %s", list);
    }

    public Object newProxyInstance(Object target) {
//        Utility.log("new ProxyInstance");
        this.target = target;
        //
        final Enhancer en = new Enhancer();
        en.setSuperclass(target.getClass());
        en.setCallback(this);
        return en.create();
    }

    public boolean condition(Point point, Method invokeMethod) {
        String selfName = point.getClass().getSimpleName();
//        Utility.log("aspect: %s", selfName);
        Class clazz = (Class) this.target.getClass();
        // Around 修饰类, 类下的所有都走 Around 里面的 AOP
        // Around 里面没写参数 , 同样也只看 method 的 Around
        if (clazz.isAnnotationPresent(Around.class)) {
            Around a = this.target.getClass().getAnnotation(Around.class);
            if (a.id().contains(selfName)) {
                return true;
            }
        }
//        Utility.log("clazz declared methods: %s", clazz);
        for (Method method : clazz.getDeclaredMethods()) {
//            Utility.log("+++ method: %s", method.getName());
            if (method.isAnnotationPresent(Around.class)) {
                Around aspect = method.getAnnotation(Around.class);
                String ids = aspect.id();
//                    Utility.log("id: %s", ids);
//                    Utility.log("equals: %s, %s", invokeMethod.getName(), method.getName());
//                    Utility.log("contains: %s", ids.contains(selfName));
                if (invokeMethod.getName().equals(method.getName()) && ids.contains(selfName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public Object proceed(Method method, Object[] objects) throws Throwable {
//        Utility.log("method: %s", method);
        Object result = null;
//        Utility.log("index: %s", index);
        if (++index == list.size()) {
            result = method.invoke(this.target, objects);
        } else {
            Point point = list.get(index);
//            Utility.log("condition: %s", condition(point, method));
            if (condition(point, method)) {
                result = point.proceed(this, method, objects);
            } else {
                result = this.proceed(method, objects);
            }
        }
//        Utility.log("result:%s", result);
        return result;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        this.index = -1;
        return this.proceed(method, objects);
    }
}
