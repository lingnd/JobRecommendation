package com.laioffer.job.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.db.MySQLConnection;
import com.laioffer.job.entity.LoginRequestBody;
import com.laioffer.job.entity.LoginResponseBody;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        LoginRequestBody body = mapper.readValue(request.getReader(), LoginRequestBody.class);
        MySQLConnection connection = new MySQLConnection();
        LoginResponseBody loginResponseBody;

        if (connection.verifyLogin(body.userId, body.password)) {
            //
            HttpSession session = request.getSession();
            session.setAttribute("user_id", body.userId);
            loginResponseBody = new LoginResponseBody("OK", body.userId, connection.getFullname(body.userId));
        } else {
            //没有用户，或者密码不对
            loginResponseBody = new LoginResponseBody("Login failed, user id and passcode do not exist.", null, null);
            response.setStatus(401);
        }
        connection.close();
        response.setContentType("application/json");
        mapper.writeValue(response.getWriter(), loginResponseBody);

    }

    //check 有没有login， login了的session就有了,只有login的时候才会创建新的session
    //所以 request.getSession(false) 自动完成了request里携带的session 信息与后端留底的session信息的比对？ yes
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        LoginResponseBody loginResponseBody;

        //session == null说明没有登陆
        if (session != null) {
            MySQLConnection connection = new MySQLConnection();
            String userId = session.getAttribute("user_id").toString();
            loginResponseBody = new LoginResponseBody("OK", userId, connection.getFullname(userId));
            connection.close();
        } else {
            loginResponseBody = new LoginResponseBody("Invalid Session.", null, null);
            response.setStatus(403);
        }
        response.setContentType("application/json");
        mapper.writeValue(response.getWriter(), loginResponseBody);
    }
}

