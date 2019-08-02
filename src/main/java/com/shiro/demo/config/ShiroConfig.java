package com.shiro.demo.config;

import com.shiro.demo.controller.LoginController;
import com.shiro.demo.shiro.RedisSessionDao;
import com.shiro.demo.shiro.UserAuthorizingRealm;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置类
 *
 * @author
 */
@Configuration
public class ShiroConfig {


    @Value("${session.redis.expireTime}")
    private long expireTime;

    /**
     * 配置安全管理器，Shiro最核心的组件
     *
     * @param userRealm
     * @return
     */
    @Bean
    public SecurityManager securityManager(UserAuthorizingRealm userRealm, RedisSessionDao redisSessionDao) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);
        // 取消Cookie中的RememberMe参数
        securityManager.setRememberMeManager(null);
        securityManager.setSessionManager(defaultWebSessionManager(redisSessionDao));
        return securityManager;
    }

    /**
     * 配置过滤器工厂，设置对应的过滤条件和跳转条件
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
        Map<String, String> filterMap = new LinkedHashMap<>();
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
        filterMap.put("/dga", "anon");
        filterMap.put("/hfdh", "anon");
        filterMap.put("/hfdhfds", "anon");
        filterMap.put("/gfdaghdfad", "anon");
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
     * 代理生成器，需要借助SpringAOP来扫描@RequiresRoles和@RequiresPermissions等注解。
     * 生成代理类实现功能增强，从而实现权限控制。
     * 需要配合AuthorizationAttributeSourceAdvisor一起使用，否则权限注解无效。
     *
     * @return
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator lifecycleBeanPostProcessor() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        // 这里需要设置为True，否则 @RequiresPermissions 注解验证不生效
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }

    /**
     * 上面配置的DefaultAdvisorAutoProxyCreator相当于一个切面，下面这个类就相当于切点了，两个一起才能实现注解权限控制。
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
     * 配置Shiro的Session管理器
     *
     * @param redisSessionDao
     * @return
     */
    @Bean
    public DefaultWebSessionManager defaultWebSessionManager(RedisSessionDao redisSessionDao) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(expireTime * 1000);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionDAO(redisSessionDao);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setDeleteInvalidSessions(true);
        /**
         * 修改Cookie中的SessionId的key，默认为JSESSIONID，自定义名称
         */
        sessionManager.setSessionIdCookie(new SimpleCookie("JSESSIONID"));
        return sessionManager;
    }

}
