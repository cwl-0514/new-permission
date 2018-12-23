package com.mmall.service;

import com.google.common.base.Preconditions;
import com.mmall.beans.PageQuery;
import com.mmall.beans.PageResult;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysUserMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysUser;
import com.mmall.param.UserParam;
import com.mmall.util.BeanValidator;
import com.mmall.util.IpUtil;
import com.mmall.util.MD5Util;
import com.mmall.util.PasswordUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SysUserService {
    @Resource
    private SysUserMapper sysUserMapper;

    /**
     * 添加用户
     * @param param
     */
    public void save(UserParam param){
        BeanValidator.check(param);
        if (checkTelephoneEixst(param.getTelephone(), param.getId())){
            throw new ParamException("电话已经被占用");
        }
        if(checkEmailEixst(param.getMail(), param.getId())){
            throw new ParamException("邮箱已经被占用");
        }
        String password = PasswordUtil.randomPassword();
         password= "12345678";
        String encrypt = MD5Util.encrypt(password);
        SysUser user = SysUser.builder().username(param.getUsername()).mail(param.getMail()).telephone(param.getTelephone())
                .remark(param.getRemark()).deptId(param.getDeptId()).password(encrypt).build();

        user.setOperator(RequestHolder.getCurrentUser().getUsername());
        user.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        user.setOperateTime(new Date());
        //发送邮件
        sysUserMapper.insertSelective(user);
    }

    public void update(UserParam param){
        BeanValidator.check(param);
        if (checkTelephoneEixst(param.getTelephone(), param.getId())){
            throw new ParamException("电话已经被占用");
        }
        if(checkEmailEixst(param.getMail(), param.getId())){
            throw new ParamException("邮箱已经被占用");
        }
        SysUser before = sysUserMapper.selectByPrimaryKey(param.getId());
        Preconditions.checkNotNull(before, "待更新的用户不存在");
        SysUser after = SysUser.builder().id(param.getId()).username(param.getUsername()).telephone(param.getTelephone()).mail(param.getMail())
                .deptId(param.getDeptId()).status(param.getStatus()).remark(param.getRemark()).build();
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        sysUserMapper.updateByPrimaryKeySelective(after);
    }

    public boolean checkEmailEixst(String email,Integer userId){
        return sysUserMapper.countByMail(email, userId) > 0;

    }
    public boolean checkTelephoneEixst(String telephone,Integer userId){
        return sysUserMapper.countByTelephone(telephone, userId) > 0;
    }

    /**
     * 查询用户
     * @param keyword
     * @return
     */
    public  SysUser findByKeyword(String keyword){
        return sysUserMapper.findByKeyword(keyword);
    }

    public PageResult<SysUser> getPageByDeptId(int deptId, PageQuery page) {
        BeanValidator.check(page);
        int count = sysUserMapper.countByDeptId(deptId);
        if (count > 0) {
            List<SysUser> list = sysUserMapper.getPageByDeptId(deptId, page);
            return PageResult.<SysUser>builder().total(count).data(list).build();
        }
        return PageResult.<SysUser>builder().build();
    }
}
