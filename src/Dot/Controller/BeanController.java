package Dot.Controller;

import Dot.Model.BeanModel;
import Dot.Model.UserModel;
import Dot.Model.UserRole;
import Dot.Service.BeanService;
import Dot.Service.UserService;
import com.alibaba.fastjson.JSONObject;
import genMVC.controller.Controller;
import genMVC.controller.Inject;
import genMVC.http.GetRequestMapping;
import genMVC.http.PostRequestMapping;
import genMVC.utils.Utility;
import org.apache.commons.beanutils.BeanUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Controller
public class BeanController {
    @Inject
    UserService userService;
    @Inject
    BeanService beanService;

    @GetRequestMapping(url = "/bean")
    public void beanPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserModel currentUser = userService.currentUser(request);
        if (currentUser.role.equals(UserRole.guest)) {
            response.sendRedirect("/user/login");
        } else {
//            request.getRequestDispatcher("/WEB-INF/template/bean.html").forward(request, response);
            Utility.returnWithPagePath(request, response, "/WEB-INF/template/bean.html");
        }
    }

    @PostRequestMapping(url = "/api/bean/add")
    public void addBean(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject data = (JSONObject) request.getAttribute("data");
        Utility.log("data: %s", data);

        UserModel currentUser = userService.currentUser(request);
        if (!currentUser.role.equals(UserRole.guest)) {
            BeanModel beanModel = new BeanModel();
            try {
                BeanUtils.populate(beanModel, data);
                beanModel.userId = currentUser.id;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            Integer beanId = beanService.add(beanModel).intValue();
            BeanModel newBean = beanService.findByBeanId(beanId);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "success");
            jsonObject.put("bean", newBean);
            Utility.returnWithData(response, jsonObject.toJSONString());
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "fail");
            jsonObject.put("cause", "loginRequired");
            Utility.returnWithData(response, jsonObject.toJSONString());
        }
    }

    @GetRequestMapping(url = "/api/bean/all")
    public void allBean(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserModel user = userService.currentUser(request);
        if (!user.role.equals(UserRole.guest)) {
            List<BeanModel> beanList = beanService.findByUserId(user.id);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "success");
            jsonObject.put("beans", beanList);
            Utility.returnWithData(response, jsonObject.toJSONString());
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "fail");
            Utility.returnWithData(response, jsonObject.toJSONString());
        }
    }

    @PostRequestMapping(url = "/api/bean/update")
    public void updateBean(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject data = (JSONObject) request.getAttribute("data");
        Utility.log("data: %s", data);

        UserModel currentUser = userService.currentUser(request);
        if (!currentUser.role.equals(UserRole.guest)) {
            BeanModel beanModel = new BeanModel();
            try {
                BeanUtils.populate(beanModel, data);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            BeanModel bean = beanService.findByBeanId(beanModel.id);
            // 判断是否有资格修改
            if (bean.userId.equals(currentUser.id)) {
                beanService.update(beanModel);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msg", "success");
                Utility.returnWithData(response, jsonObject.toJSONString());
                return;
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("msg", "fail");
                jsonObject.put("cause", "ownerRequired");
                Utility.returnWithData(response, jsonObject.toJSONString());
                return;
            }
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("msg", "fail");
            jsonObject.put("cause", "loginRequired");
            Utility.returnWithData(response, jsonObject.toJSONString());
        }
    }
}