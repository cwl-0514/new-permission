package com.mmall.common;

import com.mmall.model.SysUser;

import javax.servlet.http.HttpServletRequest;

public class RequestHolder {
    //存放用户信息 能全局获取
    private static final ThreadLocal<SysUser> userHolder = new ThreadLocal<SysUser>();
    //存放requset信息 全局获取
    private static final ThreadLocal<HttpServletRequest> requestHolder = new ThreadLocal<HttpServletRequest>();

    //向ThreadLocal 添加值
    public static void add(SysUser sysUser) {
        userHolder.set(sysUser);
    }

    public static void add(HttpServletRequest request) {
        requestHolder.set(request);
    }

    public static SysUser getCurrentUser() {
        return userHolder.get();
    }

    public static HttpServletRequest getCurrentRequest() {
        return requestHolder.get();
    }

    public static void remove() {
        userHolder.remove();
        requestHolder.remove();
    }
}
