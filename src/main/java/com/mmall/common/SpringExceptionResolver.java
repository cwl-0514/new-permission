package com.mmall.common;

import com.mmall.exception.ParamException;
import com.mmall.exception.PermissionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 接口请求全局异常处理 这个类sprimgmvc 管理配置
 */
@Slf4j
public class SpringExceptionResolver implements HandlerExceptionResolver {
    /**
     * 全局发生异常都会以拦截下来
     * @param request
     * @param response
     * @param o
     * @param ex
     * @return
     */
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object o, Exception ex) {
       //当前请求的路径
        String url = request.getRequestURI().toString();
        ModelAndView mv;
        String defaultMsg = "System error";
        //1. 页面请求: .page 2.数据请求: .json
        //只有是自己定义的异常才返回给用户 剩下都是默认系统异常
        if(url.endsWith(".json")){
            if (ex instanceof PermissionException || ex instanceof ParamException){
                //直接返回异常的信息
                JsonData jsonRsult = JsonData.fail(ex.getMessage());
                mv = new ModelAndView("jsonView", jsonRsult.toMap());
            }else {
                log.error("unkonw json exception ,url"+url,ex);
                JsonData jsonRsult = JsonData.fail(defaultMsg);
                mv = new ModelAndView("jsonView", jsonRsult.toMap());
            }
        }else if (url.endsWith(".page")){
            log.error("unkonw page exception ,url"+url,ex);
            JsonData jsonRsult = JsonData.fail(defaultMsg);
            //会到jsp页面找到 exception.jsp 异常页面
            mv = new ModelAndView("exception", jsonRsult.toMap());
        }else {
            log.error("unkonw exception ,url"+url,ex);
            JsonData jsonRsult = JsonData.fail(defaultMsg);
            mv = new ModelAndView("jsonView", jsonRsult.toMap());
        }

        return mv;
    }
}
