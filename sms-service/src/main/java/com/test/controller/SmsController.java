package com.test.controller;

import com.test.exception.ShopException;
import com.test.result.ResponseEnum;
import com.test.service.SmsService;
import com.test.util.RandomUtil;
import com.test.util.RegexValidateUtil;
import com.test.util.ResultVOUtil;
import com.test.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {
    @Autowired
    private SmsService smsService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/send/{mobile}")
    public ResultVO send(@PathVariable String mobile){
        if(mobile == null) throw new ShopException(ResponseEnum.MOBILE_NULL.getMsg());
        if (!RegexValidateUtil.checkMobile(mobile)) throw new ShopException(ResponseEnum.MOBILE_ERROR.getMsg());
        String code = RandomUtil.getSixBitRandom();
        boolean send = this.smsService.send(mobile, code);
        if(send){
            this.redisTemplate.opsForValue().set("uushop-sms-code-" + mobile,code);
            return ResultVOUtil.success("短信发送成功！");
        }
        return ResultVOUtil.fail(ResponseEnum.SMS_SEND_ERROR.getMsg());
    }
}
