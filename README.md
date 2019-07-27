首发地址：https://www.guitu18.com/post/2019/07/26/43.html



# 核心概念222

>  Apache Shiro是一个强大且易用的Java安全框架，执行身份验证、授权、密码和会话管理。使用Shiro的易于理解的API,您可以快速、轻松地获得任何应用程序,从最小的移动应用程序到最大的网络和企业应用程序。

上面这段话来自百度百科，是不是非常官方，好像说的很明白但是又好像什么都没说的样子，到底是个啥呀。想要快速理解并使用Shiro先要从最重要的三大概念入手。

1. **Subject**：大白话来讲就是用户（当然并不一定是用户，也可以指和当前应用交互的任何对象），我们在进行授权鉴权的所有操作都是围绕Subject（用户）展开的，在当前应用的任何地方都可以通过`SecurityUtils`的静态方法`getSubject()`轻松的拿到当前认证（登录）的用户。
2. **SecurityManager**：安全管理器，Shiro中最核心的组件，它管理着当前应用中所有的安全操作，包括Subject（用户），我们围绕Subject展开的所有操作都需要与SecurityManager进行交互。可以理解为SpringMVC中的前端控制器。
3. **Realms**：字面意思为领域，Shiro在进行权限操作时，需要从Realms中获取安全数据，也就是用户以及用户的角色和权限。配置Shiro，我们至少需要配置一个Realms，用于用户的认证和授权。通常我们的角色及权限信息都是存放在数据库中，所以Realms也可以算是一个权限相关的Dao层，SecurityManager在进行鉴权时会从Realms中获取权限信息。

这三个基本的概念简答理解后就可以开始配置和使用Shiro了，其实Shiro最基本的使用非常简单，加入**依赖**后只需要配置**两个Bean**，再继承一个抽象类**实现两个方法**即可。



---

# 基本使用

## 引入一个依赖

新建一个基于Springboot的Web项目，引入Shiro依赖。

```xml
        <!-- https://mvnrepository.com/artifact/org.apache.shiro/shiro-web -->
        <dependency>
            <groupId>org.apache.shiro</groupId>
            <artifactId>shiro-spring</artifactId>
            <version>1.4.0</version>
        </dependency>
```



## 配置两个Bean

新建一个Shiro配置类，配置Shiro最为核心的安全管理器SecurityManager。

```java
    @Bean
    public SecurityManager securityManager(UserAuthorizingRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(userRealm);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }
```

再配置Shiro的过滤器工厂类，将上一步配置的安全管理器注入，并配置相应的过滤规则。

```java
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 登录页面，无权限时跳转的路径
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 配置拦截规则
        Map<String, String> filterMap = new HashMap<>();
        // 首页配置放行
        filterMap.put("/", "anon");
        // 登录页面和登录请求路径需要放行
        filterMap.put("/login", "anon");
        filterMap.put("/do_login", "anon");
        // 其他未配置的所有路径都需要通过验证，否则跳转到登录页
        filterMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        return shiroFilterFactoryBean;
    }
```

上面Filter的配置顺序不能随便打乱，过滤器是按照我们配置的顺序来执行的。范围大的过滤器要放在后面，`/**`这条如果放在前面，那么一来就匹配上了，就不会继续再往后走了。这里的对上面用到的两个过滤器做一下简单说明，篇幅控制其他过滤器请参阅相关文档：

```
* authc：配置的url都必须认证通过才可以访问，它是Shiro内置的一个过滤器
* 对应的实现类 @see org.apache.shiro.web.filter.authc.FormAuthenticationFilter

* anon：也是Shiro内置的，它对应的过滤器里面是空的，什么都没做，可以理解为不拦截
* 对应的实现类 @see org.apache.shiro.web.filter.authc.AnonymousFilter
```



## 实现两个方法

在上一步的安全管理器配置中，我们通过形参注入了一个UserAuthorizingRealm对象，这个就是认证和授权相关的流程，需要我们自己实现。继承AuthorizingRealm之后，我们需要实现两个抽象方法，一个是认证，一个是授权，这两个方法长得很像，别弄混淆了。

doGetAuthenticationInfo()：认证。相当于登录，只有通过登录了，才能进行后面授权的操作。一些只需要登录权限的操作，在登录成功后就可以访问了，比如上一步中配置的`authc`过滤器就是只需要登录权限的。

doGetAuthorizationInfo()：授权。认证过后，仅仅拥有登录权限，更多细粒度的权限控制，比如菜单权限，按钮权限，甚至方法调用权限等，都可以通过授权轻松实现。在这个方法里，我们可以拿到当前登录的用户，再根据实际业务赋予用户部分或全部权限，当然这里也可以赋予用户某些角色，后面也可以根据角色鉴权。下方的演示代码仅添加了权限，赋予角色可以调用`addRoles()`或者`setRoles()`方法，传入角色集合。

```java
public class UserAuthorizingRealm extends AuthorizingRealm {

    @Autowired
    private LoginService loginService;

    /**
     * 授权验证，获取授权信息
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
        // 此处可以持久化用户的登录信息，这里仅做演示没有连接数据库
        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
    }
}
```

这样配置完成以后，就可以基于URL做粗粒度的权限控制了，我们可以通过不同的过滤器为URL配置不同的权限。

Shiro提供了很多内置的过滤器，我们最常用的就是第一个和第二个。如果对其效果不满意，我们还可以自定义过滤器实现权限控制。
文档地址：http://shiro.apache.org/web.html#default-filters

![](http://tomcat-test.guitu18.com/201907261747_398.png)



---

# 细粒度权限控制

如果需要更细致的权限控制，请继续往下添加配置，可以做到方法级别的权限控制。其实在SpringMVC中URL也能做到方法级别控制，但是使用URL来控制方法级别的权限配置起来简直反人类，通常URL权限控制通常都是泛解析，做通用的权限配置，比如后台管理的`/admin/**`这种需要登录权限的。在实际开发中注解式的权限控制用的最多。

## AdvisorAutoProxyCreator

注解式的权限控制需要配置两个Bean，第一个是AdvisorAutoProxyCreator，代理生成器，需要借助SpringAOP来扫描@RequiresRoles和@RequiresPermissions等注解，生成代理类实现功能增强，从而实现权限控制。需要配合AuthorizationAttributeSourceAdvisor一起使用，否则权限注解无效。

```java
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator autoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        autoProxyCreator.setProxyTargetClass(true);
        return autoProxyCreator;
    }
```
## AuthorizationAttributeSourceAdvisor

上面配置的DefaultAdvisorAutoProxyCreator相当于一个切面，下面这个类就相当于切点了，两个一起才能实现注解权限控制。

```java
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
```

配置完上面两个Bean之后我们就可以使用注解来控制权限了，Shiro中的权限注解有很多，我们最常用的其实就两个，@RequiresRoles和@RequiresPermissions，前者是角色验证，后者是权限验证。他们都可以传入两个参数，value是必须的，可以传入一个字符数组，表示一个或多个角色（权限），另一个参数logical有两个值可选，AND和OR，默认为AND，表示这组角色（权限）是必须都有还是仅需要一个就能访问。

举个栗子：

```java
    @RequestMapping("getLoginUserInfo")
    @RequiresPermissions(value = {"user:list", "user:info"}, logical = Logical.OR)
    public JsonResult getLoginUserInfo() {
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        return JsonResult.ok(user);
    }
```

以上代码表示`getLoginUserInfo()`方法需要当前登录用户拥有`user:list`或者`user:list`权限才能访问。

最后放上项目代码，其实代码是很早之前的，今天才做的笔记而已。

Gitee：https://gitee.com/guitu18/ShiroDemo

GitHub：https://github.com/guitu18/ShiroDemo



---

本篇结束，Shiro的使用还是非常简单的。下一篇，准备记录一下基于Springboot和Shiro使用Redis实现集群环境的Session共享，以实现单点登录。
