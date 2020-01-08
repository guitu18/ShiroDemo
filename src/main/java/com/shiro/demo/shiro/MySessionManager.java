package com.shiro.demo.shiro;

import com.shiro.demo.common.IPUtils;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * 自定义Session管理器，以Token做会话保持，同时兼容Cookie方式
 * 默认从请求头中获取Token，获取不到会继续读取Cookie
 *
 * @author zhangkuan
 * @date 2020/01/06
 */
public class MySessionManager extends DefaultWebSessionManager {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 前端ajax请求headers中须传入Authorization的值，也能兼容Cookie方式
     */
    private static final String AUTHORIZATION = "authorization";
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        String ipAddr = IPUtils.getIpAddr(httpServletRequest);
        String requestUri = httpServletRequest.getRequestURI();
        log.debug(">>>>>>>>>>>>>>>>>>>>> MySessionManager.getSessionId(), IP: {}, URI: {}", ipAddr, requestUri);
        // 先从请求头中获取 Authorization
        Serializable sessionId = httpServletRequest.getHeader(AUTHORIZATION);
        // 如果请求头中有 Authorization 则其值为sessionId
        if (sessionId != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            log.debug(">>>>>>>>>> MySessionManager.getSessionId(), 从Header中获取sessionId: " + sessionId);
            return sessionId;
        }
        // 否则按默认规则从 cookie 取sessionId
        else {
            sessionId = super.getSessionId(request, response);
            log.debug(">>>>>>>>>> MySessionManager.getSessionId(), 使用默认模式从cookie获取sessionID为: " + sessionId);
            return sessionId;
        }
    }

}
