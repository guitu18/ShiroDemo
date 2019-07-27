package com.shiro.demo.shiro;

import com.shiro.demo.entity.User;
import com.shiro.demo.service.LoginService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * UserRealm，实现认证和授权
 *
 * @author zhangkuan
 */
@Component
public class UserAuthorizingRealm extends AuthorizingRealm {

    @Autowired
    private LoginService loginService;

    /**
     * 授权验证，获取授权信息
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        User user = (User) principalCollection.getPrimaryPrincipal();
        List<String> perms;
        // 系统管理员拥有最高权限
        if (User.SUPER_ADMIN == user.getId()) {
            perms = loginService.getAllPerms();
        } else {
            perms = loginService.getUserPerms(user.getId());
        }

        // 权限Set集合
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            permsSet.addAll(Arrays.asList(perm.trim().split(",")));
        }

        // 返回权限
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 登录验证，获取身份信息
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        // 获取用户
        User user = loginService.getUserByUsername(token.getUsername());
        if (user == null) {
            throw new UnknownAccountException("账号或密码不正确");
        }
        // 判断用户是否被锁定
        if (user.getStatus() == null || user.getStatus() == 1) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        // 验证密码
        if (!user.getPassword().equals(new String(token.getPassword()))) {
            throw new UnknownAccountException("账号或密码不正确");
        }
        user.setSessionId(SecurityUtils.getSubject().getSession().getId().toString());
        // 设置最后登录时间
        user.setLastLoginTime(new Date());
        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
    }
}
