package Dot.Service;

import Dot.Mapper.UserMapper;
import Dot.Model.UserModel;
import Dot.Model.UserRole;
import genMVC.utils.Utility;
import genMVC.controller.Inject;
import genMVC.service.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class UserService {
    @Inject
    public UserMapper userMapper;

    public List<UserModel> all() {
        return userMapper.all();
    }

    public UserModel findByUsernameAndPassword(UserModel userModel) {
        List<UserModel> res = userMapper.findByUsernameAndPassword(userModel);
        if (res == null || res.size() == 0) {
            return null;
        }
        return res.get(0);
    }

    public UserModel loginValid(UserModel u) {
        u.password = saltedPassword(u.password);
        return findByUsernameAndPassword(u);
    }

    public void add(UserModel u) {
        u.password = saltedPassword(u.password);
        userMapper.add(u);
    }

    public UserModel guest() {
        UserModel userModel = new UserModel();
        userModel.id = -1;
        userModel.username = "游客";
        userModel.password = "游客";
        userModel.role = UserRole.guest;
        return userModel;
    }

    public UserModel currentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie mySessionId = Utility.getCookies(cookies, "JSESSIONID");
        String sessionId = mySessionId.getValue();
        Utility.log("currentUser sessionId: %s", sessionId);
        if (mySessionId == null || sessionId == null || sessionId.equals("null")) {
            return this.guest();
        }
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute(sessionId);
        Utility.log("currentUser userId: %s", userId);
        if (userId == null) {
            return this.guest();
        }
        UserModel u = userMapper.findById(userId);
        Utility.log("currentUser u: %s", u);
        if (u == null) {
            return this.guest();
        } else {
            return u;
        }
    }

    public static String saltedPassword(String password) {
        String salt = "sadfsadf";
        String origin = salt + password;
        try {
            // 防止 没找到 MD5 这样的方法
            MessageDigest md = MessageDigest.getInstance("md5");
            md.update(origin.getBytes(StandardCharsets.UTF_8));

            byte[] result = md.digest();

            // 00001000
            String hex = Utility.hexFromBytes(result);

            return hex;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }
}
