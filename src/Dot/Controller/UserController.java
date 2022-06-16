package Dot.Controller;

import Dot.Model.UserModel;
import Dot.Model.UserRole;
import Dot.Service.UserService;
import cn.dsna.util.images.ValidateCode;
import com.alibaba.fastjson.JSONObject;
import genMVC.controller.Controller;
import genMVC.controller.Inject;
import genMVC.http.GetRequestMapping;
import genMVC.http.PostRequestMapping;
import genMVC.utils.Utility;
import org.apache.commons.beanutils.BeanUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@Controller
public class UserController {
    @Inject
    UserService userService;

    @GetRequestMapping(url = "/user/login")
    public void login(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserModel userModel = userService.currentUser(req);
        Utility.log("userModel: %s", userModel);
        req.setAttribute("u", userModel);
        Utility.log("login page role: %s, %s", userModel.role, userModel.role.equals(UserRole.guest));
        if (userModel.role.equals(UserRole.guest)) {
            Utility.returnWithPagePath(req, resp, "/WEB-INF/template/login.html");
//            req.getRequestDispatcher("/WEB-INF/template/login.html").forward(req, resp);
        } else {
            resp.sendRedirect("/bean");
        }
    }

    @GetRequestMapping(url = "/user/register")
    public void register(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        req.getRequestDispatcher("/WEB-INF/template/register.html").forward(req, resp);
        Utility.returnWithPagePath(req, resp, "/WEB-INF/template/register.html");
    }

    @GetRequestMapping(url = "/verify_code")
    public void verifyCode(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ValidateCode validateCode = new ValidateCode(100, 30, 4, 1);
        String code = validateCode.getCode().toLowerCase();
        Utility.log("code: %s", code);
        req.getSession().setAttribute("verify_code", code);
        ServletOutputStream os = resp.getOutputStream();
        validateCode.write(os);
        os.close();
    }

    @PostRequestMapping(url = "/api/login")
    public void loginAjax(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject data = (JSONObject) request.getAttribute("data");
        Utility.log("data: %s", data);
        HttpSession session = request.getSession();
        String verifyCode = data.getString("verify_code").toLowerCase();
        String trueCode = String.valueOf(session.getAttribute("verify_code"));
        // 验证码校验
        if (trueCode.equals(verifyCode)) {
            UserModel userModel = new UserModel();
            try {
                BeanUtils.populate(userModel, data);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            UserModel hasUser = userService.loginValid(userModel);
            if (hasUser != null) {
                String sessionId = session.getId();
                Cookie cookie = new Cookie("JSESSIONID", sessionId);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(60 * 60 * 24 * 7);
                session.setMaxInactiveInterval(60 * 60 * 24 * 7);
                session.setAttribute(sessionId, hasUser.id);
                response.addCookie(cookie);
                // 成功
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msg", "success");
                Utility.returnWithData(response, jsonObject.toJSONString());
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "fail");
            jsonObject.put("cause", "username or password");
            Utility.returnWithData(response, jsonObject.toJSONString());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", "fail");
        jsonObject.put("cause", "verify_code");
        Utility.returnWithData(response, jsonObject.toJSONString());
    }

    @PostRequestMapping(url = "/api/register")
    public void registerAjax(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject data = (JSONObject) request.getAttribute("data");
        Utility.log("data: %s", data);
        HttpSession session = request.getSession();
        String verifyCode = data.getString("verify_code").toLowerCase();
        String trueCode = String.valueOf(session.getAttribute("verify_code"));
        // 验证码校验
        if (trueCode.equals(verifyCode)) {
            UserModel userModel = new UserModel();
            try {
                BeanUtils.populate(userModel, data);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            userService.add(userModel);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "success");
            Utility.returnWithData(response, jsonObject.toJSONString());
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", "fail");
        jsonObject.put("cause", "verify_code");
        Utility.returnWithData(response, jsonObject.toJSONString());
    }
}

