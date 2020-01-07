//package com.shiro.demo.shiro;
//
//import com.alibaba.fastjson.JSONObject;
//import com.shiro.demo.common.JsonResult;
//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.subject.Subject;
//import org.apache.shiro.web.filter.AccessControlFilter;
//
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
///**
// * 自定义登录过滤器
// *
// * @author zhangkuan
// * @date 2020/01/06
// */
//public class AuthLoginFilter extends AccessControlFilter {
//
//    /**
//     * 未登录登陆返状态回码
//     */
//    private int code;
//
//    /**
//     * 未登录登陆返提示信息
//     */
//    private String message;
//
//    public AuthLoginFilter(int code, String message) {
//        this.code = code;
//        this.message = message;
//    }
//
//    @Override
//    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
//        Subject subject = SecurityUtils.getSubject();
//        if (subject != null) {
//            if (subject.isRemembered()) {
//                // TODO Something
//                return Boolean.TRUE;
//            }
//            if (subject.isAuthenticated()) {
//                // TODO Something
//                return Boolean.TRUE;
//            }
//        }
//        return Boolean.FALSE;
//    }
//
//    @Override
//    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
//        PrintWriter out = null;
//        try {
//            servletResponse.setCharacterEncoding("UTF-8");
//            servletResponse.setContentType("application/json; charset=utf-8");
//            out = servletResponse.getWriter();
//            out.write(JSONObject.toJSONString(JsonResult.error(message, code)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (out != null) {
//                out.close();
//            }
//        }
//        return Boolean.FALSE;
//    }
//
//}
