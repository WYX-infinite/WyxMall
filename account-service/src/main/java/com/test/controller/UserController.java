package com.test.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.test.entity.User;
import com.test.exception.ShopException;
import com.test.form.UserLoginForm;
import com.test.form.UserRegisterForm;
import com.test.result.ResponseEnum;
import com.test.service.UserService;
import com.test.util.JwtUtil;
import com.test.util.MD5Util;
import com.test.util.RegexValidateUtil;
import com.test.util.ResultVOUtil;
import com.test.vo.ResultVO;
import com.test.vo.UserLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wyx
 * @since 2026-08-21
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    public ResultVO register(@RequestBody UserRegisterForm userRegisterForm) {
        //验证码是否正确
        //手机号格式是否正确
        String mobile = userRegisterForm.getMobile();
        if(mobile == null) throw new ShopException(ResponseEnum.MOBILE_NULL.getMsg());
        String password = userRegisterForm.getPassword();
        if(password == null) throw new ShopException(ResponseEnum.PASSWORD_NULL.getMsg());
        String code = userRegisterForm.getCode();
        if(code == null) throw new ShopException(ResponseEnum.CODE_NULL.getMsg());
        String redisCode = (String) this.redisTemplate.opsForValue().get("uushop-sms-code-" + mobile);
        if(!redisCode.equals(code)) throw new ShopException(ResponseEnum.CODE_ERROR.getMsg());
        boolean checkMobile = RegexValidateUtil.checkMobile(mobile);
        if(!checkMobile) throw new ShopException(ResponseEnum.MOBILE_ERROR.getMsg());
        //手机号是否已经被注册过
        User user = this.userService.getOne(new QueryWrapper<User>().eq("mobile", mobile));
        if(user != null) throw new ShopException(ResponseEnum.MOBILE_EXIST.getMsg());
        //密码加密
        User user1 = new User();
        String saltMD5 = MD5Util.getSaltMD5(userRegisterForm.getPassword());
        user1.setPassword(saltMD5);
        user1.setMobile(mobile);
        boolean save = this.userService.save(user1);
        if(!save) return ResultVOUtil.fail(ResponseEnum.USER_REGISTER_ERROR.getMsg());
        return ResultVOUtil.success(null);
    }

    @GetMapping("/login")
    public ResultVO login(UserLoginForm userLoginForm) {
        String mobile = userLoginForm.getMobile();
        if(mobile == null) throw new ShopException(ResponseEnum.MOBILE_NULL.getMsg());
        String password = userLoginForm.getPassword();
        if(password == null) throw new ShopException(ResponseEnum.PASSWORD_NULL.getMsg());
        //验证手机号是否与数据库一致
        User user = this.userService.getOne(new QueryWrapper<User>().eq("mobile", mobile));
        if(user == null) throw new ShopException(ResponseEnum.MOBILE_ERROR.getMsg());
        //验证密码是否与数据库一致
        boolean saltverifyMD5 = MD5Util.getSaltverifyMD5(userLoginForm.getPassword(), user.getPassword());
        if(!saltverifyMD5) throw new ShopException(ResponseEnum.PASSWORD_ERROR.getMsg());
        String token = JwtUtil.createToken(user.getUserId(), user.getMobile());
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setToken(token);
        return ResultVOUtil.success(userLoginVO);
    }

    @GetMapping("/checkToken")
    public ResultVO checkToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        boolean checkToken = JwtUtil.checkToken(token);
        if(!checkToken) return ResultVOUtil.fail(ResponseEnum.USER_TOKEN_ERROR.getMsg());
        return ResultVOUtil.success(null);
    }

}
