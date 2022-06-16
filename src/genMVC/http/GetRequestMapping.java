package genMVC.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 包名:com.hjr.framework
 * @author zyh
 * 日期2020-11-01  11:58
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetRequestMapping {
    /**
     * 映射路径
     * @return
     */
    String url();
}
