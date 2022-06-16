package genMVC;

import genMVC.utils.ClassUtil;
import genMVC.utils.Utility;
import org.apache.log4j.PropertyConfigurator;

import java.util.List;
import java.util.Properties;


public class Configuration {
    static {
        init();
    }
    public static String project;
    public static String url;
    public static String admin;
    public static String adminPassword;
    public static String driver;
    public static List<Class<?>> classList;

    public Configuration() {
    }

    public static void init() {
        try {
            Properties properties = Utility.loadResource("application.properties");
            project = properties.getProperty("project");
            url = properties.getProperty("url");
            admin = properties.getProperty("admin");
            adminPassword = properties.getProperty("adminPassword");
            driver = properties.getProperty("driver");
            classList = ClassUtil.getClasses(project);
            // log4j 配置
            PropertyConfigurator.configure(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
