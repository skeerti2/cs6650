package cs6650hw1.hw1server;

import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
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
        String reqBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        System.out.println("request body" + reqBody);
        System.out.println(s);
        if (!isUrlValidPost(s)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
//            response.setContentType("application/json");
//            Gson gson = new Gson();
            response.setStatus(HttpServletResponse.SC_OK);
            //response.getWriter().write("It works!");
        }
    }

    private boolean isUrlValidGet(String urlPath) {
        System.out.println(urlPath);
        if(urlPath.equals("/")){
            return true;
        }else if(urlPath.split("/").length == 8){
            Pattern p = Pattern.compile("^\\/[0-9]*\\/seasons\\/[0-9]*\\/days\\/[0-9]*\\/skiers\\/[0-9]*");
            Matcher m = p.matcher(urlPath);
            if(m.matches()){
                return true;
            }
        }else if(urlPath.split("/").length == 3){
            Pattern p = Pattern.compile("^\\/[0-9]*\\/vertical");
            Matcher m = p.matcher(urlPath);
            if(m.matches()){
                return true;
            }
        }
        return false;
    }


    private boolean isUrlValidPost(String urlPath) {
        if (urlPath.split("/").length == 8) {
            Pattern p = Pattern.compile("^\\/[0-9]*\\/seasons\\/[0-9]*\\/days\\/[0-9]*\\/skiers\\/[0-9]*");
            Matcher m = p.matcher(urlPath);
            if (m.matches()) {
                System.out.println("Matches!!");
                return true;
            }
        }
        return false;
    }
}
