package com.mmall.service;

import com.google.common.base.Preconditions;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysDeptMapper;
import com.mmall.exception.ParamException;
import com.mmall.model.SysDept;
import com.mmall.param.DeptParam;
import com.mmall.util.BeanValidator;
import com.mmall.util.IpUtil;
import com.mmall.util.LevelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    /**
     * 部门的添加
     * @param deptParam
     */
    public void save(DeptParam deptParam) {
        //检验参数是否出现异常
        BeanValidator.check(deptParam);
        //检验在同一级目录下是否有相同名称的部门

        if (checkExist(deptParam.getParentId(), deptParam.getName(), deptParam.getId())) {
            throw new ParamException("同一级目录下存在相同名称的部门");
        }
        SysDept dept = SysDept.builder().name(deptParam.getName()).parentId(deptParam.getParentId()).seq(deptParam.getSeq())
                .remark(deptParam.getRemark()).build();
        dept.setLevel(LevelUtil.calculateLevel(getParentLevel(deptParam.getParentId()), deptParam.getParentId()));
        dept.setOperator(RequestHolder.getCurrentUser().getUsername());
        dept.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        dept.setOperateTime(new Date());
        //保存
        sysDeptMapper.insertSelective(dept);
    }

    /**
     * 部门跟新
     * @param deptParam
     */
    public void update(DeptParam deptParam) {
        //检验参数是否出现异常
        BeanValidator.check(deptParam);
        //检验在同一级目录下是否有相同名称的部门
        if (checkExist(deptParam.getParentId(), deptParam.getName(), deptParam.getId())) {
            throw new ParamException("同一级目录下存在相同名称的部门");
        }
        SysDept before = sysDeptMapper.selectByPrimaryKey(deptParam.getId());
        /*===google 工具 会抛出异常*/
        Preconditions.checkNotNull(before, "要跟新的部门不存在");

        SysDept after = SysDept.builder().id(deptParam.getId()).name(deptParam.getName()).parentId(deptParam.getParentId())
                .seq(deptParam.getSeq()).remark(deptParam.getRemark()).build();
        after.setLevel(LevelUtil.calculateLevel(getParentLevel(deptParam.getParentId()), deptParam.getParentId()));
        after.setOperator(RequestHolder.getCurrentUser().getUsername());
        after.setOperateIp(IpUtil.getRemoteIp(RequestHolder.getCurrentRequest()));
        after.setOperateTime(new Date());
        //跟新当前部门
        updateWithChild(before, after);
    }

    /**
     * 更新当前部门下的子部门
     * @param before
     * @param after
     */
    @Transactional
    public void updateWithChild(SysDept before, SysDept after){
        String newLevelPrefix = after.getLevel();
        String oldLevelPrefix = before.getLevel();
        //当前部门levle改变了,子类level也会随之改变.
        //更新level 和更新后的level 不一致情况下 更新当前部门下的子部门 因为跟新后的部门level是再次计算出来的
        if (!before.getLevel().equals(after.getLevel())){
            //获取当前下部门所有的子部门
            List<SysDept> deptList = sysDeptMapper.getChildDeptListByLevel(before.getLevel());
            if (CollectionUtils.isNotEmpty(deptList)){
                for (SysDept dept:deptList){
                    String level = dept.getLevel();
                    if (level.indexOf(oldLevelPrefix)==0){
                        level = newLevelPrefix + level.substring(oldLevelPrefix.length());//加上部分就是有子类
                        dept.setLevel(level);
                    }
                }
                //更新当前要跟新的子部门
                sysDeptMapper.batchUpdateLevel(deptList);
            }
        }
        //更新部门
        sysDeptMapper.updateByPrimaryKey(after);

    }
    /**
     * 判断同一级下是否有相同的名字-
     * @param parentId 上级目录 增加时候使用
     * @param name 部门名字
     * @param deptId 部门id 更新时候使用
     * @return
     */
    private boolean checkExist(Integer parentId, String name, Integer deptId){
        return sysDeptMapper.countByNameAndParentId(parentId, name,deptId)>0;
    }

    /**
     * 返回父目录的level
     * @param deptId 父目录的id
     * @return
     */
    private String getParentLevel(Integer deptId){
        SysDept dept = sysDeptMapper.selectByPrimaryKey(deptId);
        if(dept == null){
            return null;
        }
        return dept.getLevel();
    }
}
