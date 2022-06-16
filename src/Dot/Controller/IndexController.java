package Dot.Controller;

import genMVC.utils.Utility;
import genMVC.controller.Controller;
import genMVC.http.GetRequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class IndexController {

    @GetRequestMapping(url = "/")
    public void updateBean(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Utility.returnWithPagePath(request, response, "/WEB-INF/template/index.html");
    }
}
