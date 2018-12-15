package com.mmall.dto;

import com.google.common.collect.Lists;
import com.mmall.model.SysDept;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * 部门层级树的定义
 */
@Getter
@Setter
@ToString
public class DeptLevelDto extends SysDept {

    private List<DeptLevelDto> deptList = Lists.newArrayList();

    public static DeptLevelDto adapt(SysDept sysDept){
        DeptLevelDto deptLevelDto = new DeptLevelDto();
        BeanUtils.copyProperties(sysDept, deptLevelDto);
        return deptLevelDto;
    }
}
