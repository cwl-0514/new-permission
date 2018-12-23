package com.mmall.service;

import com.google.common.collect.Lists;
import com.mmall.beans.CacheKeyConstants;
import com.mmall.common.RequestHolder;
import com.mmall.dao.SysAclMapper;
import com.mmall.dao.SysRoleAclMapper;
import com.mmall.dao.SysRoleUserMapper;
import com.mmall.model.SysAcl;
import com.mmall.util.JsonMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysCoreService {

    @Autowired
    private SysAclMapper sysAclMapper;
    @Autowired
    private SysRoleUserMapper sysRoleUserMapper;
    @Autowired
    private SysRoleAclMapper sysRoleAclMapper;

    //1.当前用户权限集合
    public List<SysAcl> getCurrentUserAclList(){
        //
        int userId = RequestHolder.getCurrentUser().getId();
        return getUserAclList(userId);
    }

    //2.当前角色分配权限集合
    public List<SysAcl> getRoleAclList(int roleId){
        List<Integer> aclIdList= sysRoleAclMapper.getAclIdListByRoleIdList(Lists.newArrayList(roleId));
        if(org.apache.commons.collections.CollectionUtils.isEmpty(aclIdList)){
            return Lists.newArrayList();
        }
        return  sysAclMapper.getByIdList(aclIdList);
    }

    //当前用户权限集合
    public List<SysAcl> getUserAclList(int userId){
        if(isSuperAdmin()){
            //所有的权限
            return sysAclMapper.getAll();
        }
        //当前用户角色id的集合
        List<Integer> userRoleIdList = sysRoleUserMapper.getRoleIdListByUserId(userId);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(userRoleIdList)){
            return Lists.newArrayList();
        }
        //当前用户权限的ids
        List<Integer> userAclIdList = sysRoleAclMapper.getAclIdListByRoleIdList(userRoleIdList);
        if(org.apache.commons.collections.CollectionUtils.isEmpty(userAclIdList)){
            return  Lists.newArrayList();
        }
        return sysAclMapper.getByIdList(userAclIdList);

    }
    //判断当前用户是否是超级用户
    public boolean isSuperAdmin(){
        return true;
    }

    //判断一个用户是否有访问url的权限
    public boolean hasUrlAcl(String url) {
        if (isSuperAdmin()) {
            return true;
        }
        List<SysAcl> aclList = sysAclMapper.getByUrl(url);
        if (CollectionUtils.isEmpty(aclList)) {
            return true;
        }

        List<SysAcl> userAclList = getCurrentUserAclListFromCache();
        Set<Integer> userAclIdSet = userAclList.stream().map(acl -> acl.getId()).collect(Collectors.toSet());

        boolean hasValidAcl = false;
        // 规则：只要有一个权限点有权限，那么我们就认为有访问权限
        for (SysAcl acl : aclList) {
            // 判断一个用户是否具有某个权限点的访问权限
            if (acl == null || acl.getStatus() != 1) { // 权限点无效
                continue;
            }
            hasValidAcl = true;
            if (userAclIdSet.contains(acl.getId())) {
                return true;
            }
        }
        if (!hasValidAcl) {
            return true;
        }
        return false;
    }

    public List<SysAcl> getCurrentUserAclListFromCache() {
        int userId = RequestHolder.getCurrentUser().getId();
        String cacheValue = sysCacheService.getFromCache(CacheKeyConstants.USER_ACLS, String.valueOf(userId));
        if (StringUtils.isBlank(cacheValue)) {
            List<SysAcl> aclList = getCurrentUserAclList();
            if (CollectionUtils.isNotEmpty(aclList)) {
                sysCacheService.saveCache(JsonMapper.obj2String(aclList), 600, CacheKeyConstants.USER_ACLS, String.valueOf(userId));
            }
            return aclList;
        }
        return JsonMapper.string2Obj(cacheValue, new TypeReference<List<SysAcl>>() {
        });
    }
}
