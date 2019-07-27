package com.shiro.demo.config;

import com.shiro.demo.controller.LoginController;
import com.shiro.demo.shiro.UserAuthorizingRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Shiro配置类
 *
 * @author
 */
@Configuration
public class ShiroConfig {

    /**
     * 权限管理，配置主要是Realm的管理认证
     *
     * @param userRealm
     * @return
     */
    @Bean
    public SecurityManager securityManager(UserAuthorizingRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }

    /**
     * Filter工厂，设置对应的过滤条件和跳转条件
     *
     * @param securityManager
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 登录，无权限是跳转的路径
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后跳转的路径
        shiroFilterFactoryBean.setSuccessUrl("/info");
        // 错误页面，认证不通过跳转
        shiroFilterFactoryBean.setUnauthorizedUrl("/error");
        // 配置拦截规则
        Map<String, String> filterMap = new HashMap<>();
        /**
         * authc：该过滤器下的页面必须验证后才能访问，它是Shiro内置的一个拦截器
         * @see org.apache.shiro.web.filter.authc.FormAuthenticationFilter
         * anon：它对应的过滤器里面是空的，什么都没做，可以理解为不拦截
         * @see org.apache.shiro.web.filter.authc.AnonymousFilter
         * authc：所有url都必须认证通过才可以访问；anon：所有url都都可以匿名访问
         */
        // 首页配置放行
        filterMap.put("/", "anon");
        filterMap.put("/index", "anon");
        // 登录页面和登录请求路径需要放行
        filterMap.put("/login", "anon");
        filterMap.put("/do_login", "anon");
        /**
         * "/do_logout"是退出方法，通常我们需要在退出时执行一些自定义操作
         * @see LoginController#doLogout()，如：记录日志资源回收等
         * 此处如果配置 filterMap.put("/do_logout", "logout");
         * 那么退出操作将会被Shiro接管，不会走到我们自定义的退出方法
         */
        //filterMap.put("/do_logout", "logout");
        // 未配置的所有路径都需要通过验证，否则跳转到登录页
        filterMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 加入注解的使用，不加入这个注解不生效
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    /**
     * 开启Shiro的注解(如@RequiresRoles，@RequiresPermissions)，需借助SpringAOP扫描使用Shiro注解的类，并在必要时进行安全逻辑验证
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator(可选)和AuthorizationAttributeSourceAdvisor)即可实现此功能
     *
     * @return
     */
    @Bean("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator lifecycleBeanPostProcessor() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        // 这里需要设置为True，否则 @RequiresPermissions 注解验证不生效
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

}
