package com.shiro.demo.entity;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author zhangkuan
 * @date 2019/7/29
 */
@Component
public class MyBean implements FactoryBean {

    private String message;

    public MyBean() {
        this.message = "通过构造方法初始化实例";
    }

    @Override
    public Object getObject() throws Exception {
        MyBean myBean = new MyBean();
        myBean.message = "通过FactoryBean.getObject()初始化实例";
        return myBean;
    }

    @Override
    public Class<?> getObjectType() {
        return MyBean.class;
    }

    public String getMessage() {
        return message;
    }
}
