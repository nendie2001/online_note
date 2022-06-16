package genMVC.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import genMVC.Configuration;
import genMVC.utils.Utility;
import genMVC.aspect.BaseAspect;
import genMVC.controller.Controller;
import genMVC.controller.Inject;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@WebServlet(urlPatterns = "/", loadOnStartup = 1)
public class Dispatcher extends HttpServlet {
    private Map<String, BaseMethod> postMap = new HashMap<>();
    private Map<String, BaseMethod> getMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        List<Class<?>> classList = Configuration.classList;
        try {
            if (classList == null) {
                return;
            }
            for (Class<?> clazz : classList) {
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // 创建类的对象, 自动注入
                    Object object = this.injectAll(clazz, Inject.class);
                    // 给 controller 加上代理
                    BaseAspect baseAspect = new BaseAspect();
                    Object proxyObj = baseAspect.newProxyInstance(object);
                    // 获取该字节码对象对应的类中的所有 public 方法
                    Method[] methods = clazz.getMethods();
                    // 遍历出每一个公有方法
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(GetRequestMapping.class)) {
                            String requestMappingValue = method.getAnnotation(GetRequestMapping.class).url();
                            getMap.put(requestMappingValue, new BaseMethod(proxyObj, method));
                        } else if (method.isAnnotationPresent(PostRequestMapping.class)) {
                            String requestMappingValue = method.getAnnotation(PostRequestMapping.class).url();
                            postMap.put(requestMappingValue, new BaseMethod(proxyObj, method));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        try {
            String uri = request.getRequestURI();
//            Utility.log("get uri: %s, getMap: %s", uri, this.postMap);
            BaseMethod baseMethod = postMap.get(uri);
            if (baseMethod != null) {
                Utility.log("[POST] Path: %s", uri);
                BufferedReader reader = request.getReader();
                String json = reader.readLine();
                JSONObject jsonObject = JSON.parseObject(json);
                reader.close();
                Utility.log("[POST] Data: %s", jsonObject.toJSONString());
                request.setAttribute("data", jsonObject);
                baseMethod.getMethod().invoke(baseMethod.getObject(), request, response);
            } else {
                Utility.returnWithPagePath(request, response, "/WEB-INF/template/error.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try {
            String uri = request.getRequestURI();
//            Utility.log("get uri: %s, getMap: %s", uri, this.getMap);
            BaseMethod baseMethod = getMap.get(uri);
            if (baseMethod != null) {
                Utility.log("[GET] Path: %s", uri);
                baseMethod.getMethod().invoke(baseMethod.getObject(), request, response);
            } else {
                Utility.returnWithPagePath(request, response, "/WEB-INF/template/error.html");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public Object newClass(Class c) throws Exception {
        Object o = c.getDeclaredConstructor().newInstance();
        return o;
    }

    public Object injectAll(Class<?> clazz, Class annotation) throws Exception {
        if (clazz == null) {
            return null;
        }
//        Utility.log("clazz: %s", clazz);
        Object o = this.newClass(clazz);
        Field[] fs = clazz.getDeclaredFields();
        for (Field f : fs) {
            f.setAccessible(true);
            if (f.isAnnotationPresent(annotation)) {
                Class<?> fClass = f.getType();
                // 实例化 baseAspect
                Object baseAspectObj = this.newClass(BaseAspect.class);
                Method newProxyInstance = baseAspectObj.getClass().getDeclaredMethod("newProxyInstance", Object.class);
                // 实例化 service
                Object serviceObj = injectAll(fClass, Inject.class);
                Object proxyObj = newProxyInstance.invoke(baseAspectObj, serviceObj);
                //
                f.set(o, proxyObj);
            }
        }
//        Utility.log("return o: %s", o);
        return o;
    }
}
