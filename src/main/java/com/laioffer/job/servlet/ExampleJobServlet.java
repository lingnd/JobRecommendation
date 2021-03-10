package com.laioffer.job.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.entity.ExampleCoordinates;
import com.laioffer.job.entity.ExampleJob;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ExampleJobServlet", urlPatterns = {"/example_job"})
public class ExampleJobServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //不写的话，输出没差别，有的client可以自动detect是什么格式.但是要写出来，告诉你的client，你用的什么格式
        response.setContentType("application/json");
        //做mapper的class，属于Jaskson databind这个class
        ObjectMapper mapper= new ObjectMapper();
        //创建两个model class 的 object
        ExampleCoordinates coordinates = new ExampleCoordinates(37.485130, -122.148316);
        ExampleJob job = new ExampleJob("Software Engineer", 123456, "Aug 1, 2020", false, coordinates);
        //写出去mapper.writeValueAsString() return 的是string
        response.getWriter().print(mapper.writeValueAsString(job));
    }
}

