package cs6650hw1.hw1server;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "ResortServlet", value = "/resorts/*")
public class ResortServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String s = request.getPathInfo();
        if (!isUrlValidGet(s)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("It works!");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String s = request.getPathInfo();
        if (!isUrlValidPost(s)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("It works!");
        }
    }

    private boolean isUrlValidGet(String urlPath) {
        if(urlPath.equals("/")){
            return true;
        }else if(urlPath.split("/").length == 7){
            Pattern p = Pattern.compile("^\\/[0-9]*\\/seasons\\/[0-9]*\\/day\\/[0-9]*\\/skiers");
            Matcher m = p.matcher(urlPath);
            if(m.matches()){
                return true;
            }
        }else if(urlPath.split("/").length == 3){
            Pattern p = Pattern.compile("^\\/[0-9]*\\/seasons");
            Matcher m = p.matcher(urlPath);
            if(m.matches()){
                return true;
            }
        }
        return false;
    }


    private boolean isUrlValidPost(String urlPath) {
        if(urlPath.equals("/")){
            return true;
        }else if(urlPath.split("/").length == 3){
            Pattern p = Pattern.compile("^\\/[0-9]*\\/seasons");
            Matcher m = p.matcher(urlPath);
            if(m.matches()){
                return true;
            }
        }
        return false;
    }
}
