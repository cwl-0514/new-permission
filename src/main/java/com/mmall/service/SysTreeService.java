package com.mmall.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Lists;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dto.DeptLevelDto;
import com.mmall.model.SysDept;
import com.mmall.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 树层级的计算
 */
@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;


    /*======================
     * 整体流程
     * 1.取出所有的数据
     * 2.组装数据list<DeptLevelDto>
     * 3从根节点获取数据
     * 4.逐层组装成树状数据
     * ====================
     * */
    //部门转化成树状
    public List<DeptLevelDto> deptTree(){
        //取出所有部门
        List<SysDept> deptList = sysDeptMapper.getAllDept();
        List<DeptLevelDto> list = Lists.newArrayList();
        //把 List<SysDept> deptList 转换成 -->List<DeptLevelDto> list
        for (SysDept dept:deptList){
            DeptLevelDto adaptDept = DeptLevelDto.adapt(dept);
            list.add(adaptDept);
        }
        //最终转化成树形形状
        return deptListToTree(list);
    }

    /**
     * 转化成树形形状
     * @param list
     * @return
     */
    private List<DeptLevelDto> deptListToTree(List<DeptLevelDto> list){
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }
        // 开始转换这种形式 leve-->[dept1,dept2,dept3,...]
        Multimap<String, DeptLevelDto> leveDeptMap = ArrayListMultimap.create();//谷歌转换工具

        //定义root部门集合
        List<DeptLevelDto> rootList = Lists.newArrayList();

        for(DeptLevelDto dto:list){
            leveDeptMap.put(dto.getLevel(), dto);
            //这个判断是把根目录de==处理树状第一层数pt集合放到一起  据=
            if(LevelUtil.ROOT.equals(dto.getLevel())){
                rootList.add(dto);
            }
        }
        // 按照seq从小到大排序
        Collections.sort(list, new Comparator<DeptLevelDto>() {
            public int compare(DeptLevelDto o1, DeptLevelDto o2) {
                return o1.getSeq() - o2.getSeq();
            }
        });
        //递归生成树
        transformDeptTree(rootList ,LevelUtil.ROOT, leveDeptMap);
        return rootList;
    }

    /**
     *  开始转换成 树状 处理主层级下层级数据
     * @param rootList 当前层级节点数据的集合
     * @param level 以根目录 "0" 为起点
     * @param levelDeptMap key->[dept1,dept2,dept3] 形式的集合
     */
    // level:0, 0, all 0->0.1,0.2
    // level:0.1
    // level:0.2
    private void transformDeptTree(List<DeptLevelDto> rootList, String level,  Multimap<String, DeptLevelDto> levelDeptMap){
        //获取的数据已经按seq排好顺序了
        for(int i=0;i<rootList.size();i++){
            // 遍历该层的每个元素
            DeptLevelDto deptLevelDto = rootList.get(i);
            // 处理当前层级的数据 计算出下个层级(子类的level)
            String nextLevel = LevelUtil.calculateLevel(level, deptLevelDto.getId());
            // 处理下一层 key->[] level->[dept1,dept2,dept3]
            List<DeptLevelDto> tempDeptList = (List<DeptLevelDto>) levelDeptMap.get(nextLevel);//根据key 获取value
            if (CollectionUtils.isNotEmpty(tempDeptList)){
                //排序
                Collections.sort(tempDeptList, deptSeqComparator);
                //设置下一层部门数据集合(拿到子类集合数据)
                deptLevelDto.setDeptList(tempDeptList);//向父部门添加子部门
                //进入到下一层处理
                transformDeptTree(tempDeptList, nextLevel, levelDeptMap);
            }
        }
    }
    //部门排序
    public Comparator<DeptLevelDto> deptSeqComparator = new Comparator<DeptLevelDto>() {
        public int compare(DeptLevelDto o1, DeptLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };


}
