package Dot.Controller;

import genMVC.controller.Controller;
import genMVC.http.GetRequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class ImageController {

    @GetRequestMapping(url = "/image")
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletContext servletContext = req.getServletContext();
        String imageName = req.getParameter("name");
        String path = String.format("static/img/%s", imageName);
        InputStream is = servletContext.getResourceAsStream(path);
        ServletOutputStream os = resp.getOutputStream();
        byte[] bytes = is.readAllBytes();
        os.write(bytes);
        os.close();
        is.close();
    }
}
