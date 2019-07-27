package com.shiro.demo.controller;

import com.shiro.demo.common.JsonResult;
import com.shiro.demo.entity.User;
import com.shiro.demo.exception.MyException;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Index
 *
 * @author zhangkuan
 */
@RestController
public class IndexController {

    private Logger log = Logger.getLogger(this.getClass());

    @RequestMapping({"index", ""})
    public JsonResult index() throws MyException {
        log.debug("首页测试");
        // 异常测试
        int a = 10;
        if (a % new Random().nextInt(2) == 0) {
            log.error("自定义异常测试");
            throw new MyException(-1, "抛出自定义异常");
        }
        return JsonResult.ok("hello world");
    }

    @RequestMapping("login")
    public JsonResult login() {
        log.debug("登录页");
        return JsonResult.ok("login page");
    }

    @RequestMapping("info")
    @RequiresPermissions(value = {"user:list", "user:info"}, logical = Logical.OR)
    public JsonResult info() {
        log.debug("获取当前登录用户信息");
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        PrincipalCollection previousPrincipals = subject.getPreviousPrincipals();
        PrincipalCollection principals = subject.getPrincipals();
        Session session = subject.getSession();
        return JsonResult.ok(user);
    }

    @RequestMapping("list")
    @RequiresPermissions("user:list")
    public JsonResult list() {
        log.debug("获取用户列表");
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new User("user_" + i, "test_user_" + i));
        }
        return JsonResult.ok(list);
    }

}
