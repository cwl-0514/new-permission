package com.mmall.dao;

import com.mmall.beans.PageQuery;
import com.mmall.model.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);

    public int countByMail(@Param("mail") String email, @Param("id") Integer id);

    public int countByTelephone(@Param("telephone") String telephone,@Param("id") Integer id);
    /**
     *  根据用户名或者邮箱查询用户
     * @param keyword 用户名或者邮箱
     * @return
     */
    public  SysUser findByKeyword(String keyword);

    /**
     * 根据部门id查询用户的数量
     * @param deptId
     * @return
     */
    int countByDeptId(@Param("deptId") int deptId);

    /**
     * 分页查询
     * @param deptId
     * @param page
     * @return
     */
    List<SysUser> getPageByDeptId(@Param("deptId") int deptId, @Param("page") PageQuery page);

}