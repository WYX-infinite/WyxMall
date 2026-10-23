package com.test.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.test.entity.Admin;
import com.test.exception.ShopException;
import com.test.form.AdminLoginForm;
import com.test.result.ResponseEnum;
import com.test.service.AdminService;
import com.test.util.JwtUtil;
import com.test.util.ResultVOUtil;
import com.test.vo.AdminLoginVO;
import com.test.vo.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wyx
 * @since 2026-08-21
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    /**
     * 用户登录
     * @param adminLoginForm
     * @return
     */
    @GetMapping("/login")
    public ResultVO login(AdminLoginForm adminLoginForm){
        QueryWrapper<Admin> adminQueryWrapper = new QueryWrapper<>();
        adminQueryWrapper.eq("username",adminLoginForm.getUsername());
        Admin admin = this.adminService.getOne(adminQueryWrapper);
        if(admin == null) throw new ShopException(ResponseEnum.ADMIN_USERNAME_NULL.getMsg());
        if(!admin.getPassword().equals(adminLoginForm.getPassword()))
            throw new ShopException(ResponseEnum.ADMIN_PASSWORD_ERROR.getMsg());
        String token = JwtUtil.createToken(admin.getAdminId(), admin.getUsername());
        AdminLoginVO adminLoginVO = new AdminLoginVO();
        BeanUtils.copyProperties(admin,adminLoginVO);
        adminLoginVO.setToken(token);
        return ResultVOUtil.success(adminLoginVO);
    }

    /**
     * Token验证
     * @return
     */
    @GetMapping("/checkToken")
    public ResultVO checkToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        boolean checkToken = JwtUtil.checkToken(token);
        if(!checkToken) return ResultVOUtil.fail(ResponseEnum.ADMIN_TOKEN_ERROR.getMsg());
        return ResultVOUtil.success(null);
    }

}
