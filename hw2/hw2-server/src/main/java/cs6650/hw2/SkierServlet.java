package cs6650.hw2;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(name = "SkierServlet", value = "/skiers/*")
public class SkierServlet extends HttpServlet {
    private final static String QUEUE_NAME = "hello";
    private GenericObjectPool<Channel> pool;


    @Override
    public void init() throws ServletException {
        try {
            ChannelFactory channelFactory = new ChannelFactory();
            pool = new GenericObjectPool<Channel>(channelFactory);
        } catch (IOException e) {
           throw new ServletException(e.getMessage());
        } catch (TimeoutException e) {
            throw new ServletException(e.getMessage());
        }
    }
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
            System.out.println("came here");
            Channel channel = null;
            String[] reqParamList = s.split("/");
            Gson gson = new Gson();
            System.out.println("object is:");
            SkierRide skierObj = gson.fromJson(reqBody,
                            SkierRide.class);
            skierObj.setResortID(Integer.parseInt(reqParamList[1]));
            skierObj.setSeasonID(Integer.parseInt(reqParamList[3]));
            skierObj.setDayID(Integer.parseInt(reqParamList[5]));
            skierObj.setSkierID(Integer.parseInt(reqParamList[7]));

           try {
                channel = this.pool.borrowObject();
               System.out.println("creating and borrowing channel");
                JSONObjUtil.sendMessageToQueue(skierObj, QUEUE_NAME, channel);
            } catch (IOException e) {
               System.out.println("IO Exception occured");
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to borrow buffer from pool" + e.toString());
        }finally {
               try {
                   pool.returnObject(channel);
               } catch (Exception e) {
                   System.out.println("Some error");
               }
           }
        response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("It works!");
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
