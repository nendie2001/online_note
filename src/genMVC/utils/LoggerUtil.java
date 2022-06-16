package genMVC.utils;

import net.sf.cglib.proxy.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LoggerUtil {
    private static org.slf4j.Logger logger;
    private static String FQCN = null;

    public final static org.slf4j.Logger getProxy(Class cls) {
        FQCN = cls.getName();
        try {
            Enhancer eh = new Enhancer();
            eh.setSuperclass(org.apache.log4j.Logger.class);
            eh.setCallbackType(LogInterceptor.class);
            Class c = eh.createClass();
            Enhancer.registerCallbacks(c, new LogInterceptor[]{new LogInterceptor()});

            Constructor<org.apache.log4j.Logger> constructor = c.getConstructor(String.class);
            org.apache.log4j.Logger loggerProxy = constructor.newInstance(cls.getName());

            LoggerRepository loggerRepository = LogManager.getLoggerRepository();
            org.apache.log4j.spi.LoggerFactory lf = ReflectionUtil.getFieldValue(loggerRepository, "defaultFactory");
            Object loggerFactoryProxy = Proxy.newProxyInstance(
                    LoggerFactory.class.getClassLoader(),
                    new Class[]{LoggerFactory.class},
                    new NewLoggerHandler(loggerProxy)
            );

            ReflectionUtil.setFieldValue(loggerRepository, "defaultFactory", loggerFactoryProxy);
            logger = org.slf4j.LoggerFactory.getLogger(cls.getName());
            ReflectionUtil.setFieldValue(loggerRepository, "defaultFactory", lf);
            return logger;
        } catch (
                IllegalAccessException |
                        NoSuchMethodException |
                        InvocationTargetException |
                        InstantiationException e) {
            throw new RuntimeException("初始化Logger失败", e);
        }
    }

    private static class LogInterceptor implements MethodInterceptor {
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            // 只拦截log方法。
            if (objects.length != 4 || !method.getName().equals("log"))
                return methodProxy.invokeSuper(o, objects);
            objects[0] = FQCN;
            return methodProxy.invokeSuper(o, objects);
        }
    }

    private static class NewLoggerHandler implements InvocationHandler {
        private final org.apache.log4j.Logger proxyLogger;

        public NewLoggerHandler(org.apache.log4j.Logger proxyLogger) {
            this.proxyLogger = proxyLogger;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return proxyLogger;
        }
    }
}

