package com.mmall.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysAclMapper;
import com.mmall.dao.SysAclModuleMapper;
import com.mmall.dao.SysDeptMapper;
import com.mmall.dto.AclDto;
import com.mmall.dto.AclModuleLevelDto;
import com.mmall.dto.DeptLevelDto;
import com.mmall.model.SysAcl;
import com.mmall.model.SysAclModule;
import com.mmall.model.SysDept;
import com.mmall.util.LevelUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 树层级的计算
 */
@Service
public class SysTreeService {

    @Resource
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysAclModuleMapper sysAclModuleMapper;
    @Autowired
    private SysCoreService sysCoreService;
    @Autowired
    private SysAclMapper sysAclMapper;

    public List<AclModuleLevelDto> roleTree(int roleId){
        //1.当前用户分配的权限点
        List<SysAcl> userAclList = sysCoreService.getUserAclList(RequestHolder.getCurrentUser().getId());
        //2.当前角色分配的权限点
        List<SysAcl> roleAclList = sysCoreService.getRoleAclList(roleId);
        //3.当前系统所有的权限点
        List<AclDto> aclDtoList = Lists.newArrayList();

        //把用户权限id集合存到set集合
        Set<Integer> userAclSet = userAclList.stream().map(sysAcl -> sysAcl.getId()).collect(Collectors.toSet());
        //把用户权限id集合存到set集合
        Set<Integer> roleAclSet = roleAclList.stream().map(roleAcl->roleAcl.getId()).collect(Collectors.toSet());

        //用户权限和角色权限取并集
//        Set<SysAcl> aclSet = new HashSet<>(roleAclList);
//        aclSet.addAll(userAclList);
        //获取所有的权限点
        List<SysAcl> allAclList = sysAclMapper.getAll();
        for (SysAcl sysAcl:allAclList){
            AclDto aclDto = AclDto.adapt(sysAcl);
            if(userAclSet.contains(aclDto.getId())){
                aclDto.setHasAcl(true);
            }
            if (roleAclSet.contains(aclDto.getId())){
                aclDto.setChecked(true);
            }
            aclDtoList.add(aclDto);
        }
        return aclListtoTree(aclDtoList);
    }

    /**
     * 当前用户权限点转化成树状
     * @param aclDtoList  权限点的集合
     * @return
     */
    public List<AclModuleLevelDto> aclListtoTree(List<AclDto> aclDtoList){
        if(org.apache.commons.collections.CollectionUtils.isEmpty(aclDtoList)){
            return Lists.newArrayList();
        }
        //获取权限模块的树
        List<AclModuleLevelDto> aclModuleLevelList = aclModuleTree();

        Multimap<Integer, AclDto> moduleIdAclMap = ArrayListMultimap.create();
        for(AclDto acl : aclDtoList) {
            //在有效的状态情况下
            if (acl.getStatus() == 1) {
                //一个 权限模块 的id 对应多个权限点
                moduleIdAclMap.put(acl.getAclModuleId(), acl);
            }
        }
        // 权限点绑定到权限模块上
        bindAclsWithOrder(aclModuleLevelList, moduleIdAclMap);
        return aclModuleLevelList;
    }

    /**
     * 权限点绑定到权限模块上
     * @param aclModuleLevelList 权限模块集合
     * @param moduleIdAclMap 权限点
     */
    public void bindAclsWithOrder(List<AclModuleLevelDto> aclModuleLevelList, Multimap<Integer, AclDto> moduleIdAclMap){
        if (CollectionUtils.isEmpty(aclModuleLevelList)) {
            return;
        }
        //便利所有的权限模块
        for (AclModuleLevelDto dto : aclModuleLevelList) {
            //根据权限模块的id 获取权限点 集合
            List<AclDto> aclDtoList = (List<AclDto>)moduleIdAclMap.get(dto.getId());
            if (CollectionUtils.isNotEmpty(aclDtoList)) {
                Collections.sort(aclDtoList, aclSeqComparator);
                //权限点绑定到权限模块上
                dto.setAclList(aclDtoList);
            }
            bindAclsWithOrder(dto.getAclModuleList(), moduleIdAclMap);
        }
    }
    /*======================
     * 整体流程
     * 1.取出所有的数据
     * 2.组装数据list<DeptLevelDto>
     * 3从根节点获取数据
     * 4.逐层组装成树状数据
     * ====================
     * */
    //1.部门转化成树状
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

    //2.权限模块树的取值
    public List<AclModuleLevelDto> aclModuleTree() {
        List<SysAclModule> aclModuleList = sysAclModuleMapper.getAllAclModule();
        List<AclModuleLevelDto> dtoList = Lists.newArrayList();
        for (SysAclModule aclModule : aclModuleList) {
            dtoList.add(AclModuleLevelDto.adapt(aclModule));
        }
        return aclModuleListToTree(dtoList);
    }

    public List<AclModuleLevelDto> aclModuleListToTree(List<AclModuleLevelDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return Lists.newArrayList();
        }
        // level -> [aclmodule1, aclmodule2, ...] Map<String, List<Object>>
        Multimap<String, AclModuleLevelDto> levelAclModuleMap = ArrayListMultimap.create();
        List<AclModuleLevelDto> rootList = Lists.newArrayList();
        //处理当前层级
        for (AclModuleLevelDto dto : dtoList) {
            levelAclModuleMap.put(dto.getLevel(), dto);
            if (LevelUtil.ROOT.equals(dto.getLevel())) {
                rootList.add(dto);
            }
        }
        Collections.sort(rootList, aclModuleSeqComparator);
        transformAclModuleTree(rootList, LevelUtil.ROOT, levelAclModuleMap);
        return rootList;
    }
    //处理当前层级下一级递归循环
    public void transformAclModuleTree(List<AclModuleLevelDto> dtoList, String level, Multimap<String, AclModuleLevelDto> levelAclModuleMap) {
        for (int i = 0; i < dtoList.size(); i++) {
            AclModuleLevelDto dto = dtoList.get(i);
            String nextLevel = LevelUtil.calculateLevel(level, dto.getId());
            List<AclModuleLevelDto> tempList = (List<AclModuleLevelDto>) levelAclModuleMap.get(nextLevel);
            if (CollectionUtils.isNotEmpty(tempList)) {
                Collections.sort(tempList, aclModuleSeqComparator);
                dto.setAclModuleList(tempList);
                transformAclModuleTree(tempList, nextLevel, levelAclModuleMap);
            }
        }
    }
    //权限模块排序
    public Comparator<AclModuleLevelDto> aclModuleSeqComparator = new Comparator<AclModuleLevelDto>() {
        public int compare(AclModuleLevelDto o1, AclModuleLevelDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };

    public Comparator<AclDto> aclSeqComparator = new Comparator<AclDto>() {
        public int compare(AclDto o1, AclDto o2) {
            return o1.getSeq() - o2.getSeq();
        }
    };
}
