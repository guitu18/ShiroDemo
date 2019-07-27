package com.shiro.demo.service;

import com.shiro.demo.entity.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 因为只是测试，没有连接数据库，这里通过代码模拟数据库做用户登录授权相关操作
 *
 * @author zhangkuan
 */
@Service
public class LoginService {

    public void login(User user) {
        UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), user.getPassword());
        token.setRememberMe(true);
        Subject subject = SecurityUtils.getSubject();
        subject.login(token);
    }

    public User getUserByUsername(String username) {
        return new User("zhangkuan", "zhangkuan");
    }

    /**
     * 获取所有权限
     *
     * @return
     */
    public List<String> getAllPerms() {
        List<String> list = new ArrayList<>();
        list.add("user:list");
        list.add("user:info");
        list.add("user:save");
        list.add("user:update");
        list.add("user:delete");
        return list;
    }

    /**
     * 获取用户权限
     *
     * @param userId
     * @return
     */
    public List<String> getUserPerms(Integer userId) {
        List<String> list = new ArrayList<>();
        list.add("user:list");
        list.add("user:info");
        return list;
    }
}
