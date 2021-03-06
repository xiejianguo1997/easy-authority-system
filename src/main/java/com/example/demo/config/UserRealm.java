package com.example.demo.config;

import com.alibaba.fastjson.JSON;
import com.example.demo.entity.Permisson;
import com.example.demo.entity.Role;
import com.example.demo.entity.ShiroUser;
import com.example.demo.entity.User;
import com.example.demo.service.IPermissionService;
import com.example.demo.service.IRoleService;
import com.example.demo.service.IShiroUserService;
import com.example.demo.service.IUserService;
import com.example.demo.vo.PermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:
 * @Author: boolean
 * @Date: 2020/1/31 22:09
 * 自定义的Realm
 */
@Slf4j
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private IUserService userService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IPermissionService permissionService;

    /**
     * 执行授权逻辑
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.info("执行授权逻辑");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        String name = user.getName();
        String roleName = roleService.getRole(name);
        List<PermissionVO> permissionVOS = permissionService.getPermissionList(roleName);
        permissionVOS.stream().forEach(permissionVO -> {
            if (permissionVO.getTypePath().length() > 0) {
                info.addStringPermission(permissionVO.getTypePath());
            }
        });
        log.info("获得的info:{}", info);
        return info;
    }


    /**
     * 执行认证逻辑
     *
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        log.info("执行认证逻辑");

        UsernamePasswordToken token1 = (UsernamePasswordToken) token;
        User user = userService.getUser(token1.getUsername(), String.copyValueOf(token1.getPassword()));
        // 判断用户名
        if (user == null) {
            return null; //shiro 抛出 UnaKnowAccountException
        }

        // 判断密码
        return new SimpleAuthenticationInfo(user, user.getPassword(), "");
    }
}



