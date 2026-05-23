package com.example.shift.test;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginSecurityTest {

    @GetMapping({"/","/home"})
    @ResponseBody
    public String home(){
        return "<h1> what ever</h1><a href='/dashboard'>대시보드 이동</a>";
    }
    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard(){
        return "<h1>Dashboard (로그인 성공유저) </h1><a href='/logout'>로그아웃</a>";
    }



}
