package com.mmall.controller;

import com.mmall.beans.PageQuery;
import com.mmall.beans.PageResult;
import com.mmall.common.JsonData;
import com.mmall.model.SysUser;
import com.mmall.param.DeptParam;
import com.mmall.param.UserParam;
import com.mmall.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData saveUser(UserParam userParam){
        sysUserService.save(userParam);
        return JsonData.success();
    }

    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData updateUser(UserParam userParam){
        sysUserService.update(userParam);
        return JsonData.success();
    }
    @RequestMapping("/page.json")
    @ResponseBody
    public JsonData page(@RequestParam("deptId") int deptId, PageQuery pageQuery) {
        PageResult<SysUser> result = sysUserService.getPageByDeptId(deptId, pageQuery);
        return JsonData.success(result);
    }

}
