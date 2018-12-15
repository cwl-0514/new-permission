package com.mmall.dao;

import com.mmall.model.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SysDept record);

    int insertSelective(SysDept record);

    SysDept selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SysDept record);

    int updateByPrimaryKey(SysDept record);

    /**
     * 取出所有的部门
     * @return
     */
    List<SysDept> getAllDept();

    /**
     * 获取当前部门下所有的子部门集合
     * @param level
     * @return
     */
    List<SysDept> getChildDeptListByLevel(@Param("level") String level);

    /*批量更新子部门能-*/
    void batchUpdateLevel(@Param("sysDeptList") List<SysDept> sysDeptList);

    /*判断同一级下是否有相同的名字*/
    int countByNameAndParentId(@Param("parentId") Integer parentId, @Param("name") String name, @Param("id") Integer id);

    int countByParentId(@Param("deptId") int deptId);
}