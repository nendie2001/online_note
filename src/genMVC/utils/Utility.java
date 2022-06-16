package genMVC.utils;

import com.alibaba.fastjson.JSONObject;
import genMVC.Configuration;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utility {
    public static final Logger logger = LoggerUtil.getProxy(Utility.class);

    static public void log(String format, Object... args) {
        String s = String.format(format, args);
        logger.info(s);
    }

    public static void ensure(boolean condition, String message) {
        if (!condition) {
            log("%s", message);
        } else {
            log("测试成功");
        }
    }

    public static String getType(Object o) {
        return o.getClass().toString();
    }

    public static Scanner input(String message) {
        String m = String.format("%s: ", message);
        System.out.print(m);
        Scanner scanner = new Scanner(System.in);
        return scanner;
    }

    public static int randomBetween(int start, int end) {
        // start - end (end 取不到)
        Random rand = new Random();
        // rand.nextInt(100) => 0 - 99
        return rand.nextInt(end - start) + start;
    }

    public static int[] randomArray(int size, int startIndex, int endIndex) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = randomBetween(startIndex, endIndex);
        }
        return arr;
    }

    public static <T> void shuffleArray(ArrayList<T> list) {
        ArrayList<T> s = list;
        int len = s.size();
        for (int i = len - 1; i >= 0; i--) {
            int randomIndex = randomBetween(0, i + 1);
            T temp = s.get(randomIndex);
            s.set(randomIndex, s.get(i));
            s.set(i, temp);
        }
    }

    public static String shuffleString(String str) {
        String[] strArray = str.split("");
        ArrayList<String> list = new ArrayList<>(Arrays.asList(strArray));
        shuffleArray(list);
        return list.toString().replace(", ", "");
    }

    public static String randomString(int length) {
        String rawData = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz~!@#$%^&*()_+{}|:\"<>?`-=[]\\;',./'";
        // 洗牌
        String str = shuffleString(rawData);
        int len = str.length();
        StringBuilder verifyCode = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randNumber = randomBetween(0, len);
            verifyCode.append(str.charAt(randNumber));
        }
        return verifyCode.toString();
    }

    public static Long unixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String formatTime(Long unixTime) {
        String pattern = "yyyy/mm/dd hh:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        Date date = new Date(unixTime * 1000);
        String dateString = df.format(date);
        return dateString;
    }

    public static <T> Type[] TClass(T self) {
        Type type = self.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            // 参数化类型中可能有多个泛型参数
            Type[] types = parameterizedType.getActualTypeArguments();
            return types;
        }
        return null;
    }

    public static Properties loadResource(String path) throws IOException {
        Properties properties = new Properties();
        InputStreamReader reader = new InputStreamReader(Configuration.class.getClassLoader().getResourceAsStream(path));
        properties.load(reader);
        return properties;
    }

    static public Cookie getCookies(Cookie[] cookies, String key) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    Utility.log("getCookies, sessionId: %s", cookie.getValue());
                    return cookie;
                }
            }
        }
        return new Cookie("mySessionId", "null");
    }

    public static String parseRequest(HttpServletRequest request) throws IOException {
        Utility.log("enter parse");
        BufferedReader reader = request.getReader();
        String json = reader.readLine();
        JSONObject jsonResult = JSONObject.parseObject(json);
        Utility.log("json: %s", json);
        reader.close();
        Utility.log("finish parse");
        return "json";
    }

    public static void returnWithData(HttpServletResponse response, String data) throws IOException {
        Utility.log("return data: %s", data);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write(data);
        out.close();
    }

    public static String hexFromBytes(byte[] array) {
        String hex = new BigInteger(1, array).toString(16);
        int zeroLength = array.length * 2 - hex.length();
        for (int i = 0; i < zeroLength; i++) {
            hex = "0" + hex;
        }
        return hex;
    }

    public static void returnWithPagePath(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        ServletContext servletContext = request.getServletContext();
        String filePath = servletContext.getRealPath(path);
        FileInputStream fis = new FileInputStream(filePath);
        ServletOutputStream os = response.getOutputStream();
        byte[] bytes = fis.readAllBytes();
        os.write(bytes);
        os.close();
        fis.close();
    }
}
