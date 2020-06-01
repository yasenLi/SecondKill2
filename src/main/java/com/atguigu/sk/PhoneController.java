package com.atguigu.sk;

import com.atguigu.sk.util.JedisPoolUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class PhoneController {

    @PostMapping(value = "/getCode",produces = "text/html;charset=utf-8")
    public String getCode(String phone){
        int code =(int)((Math.random()*9+1)*100000);
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();

        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        Date date = new Date();
        String dates = dateFormat.format(date);

        String codeKey =phone+":"+dates+":code";
        String countKey=phone+":count";
        System.out.println(codeKey+"======>"+code);
        //获取指定的电话号码发送的验证码次数

        String count = jedis.get(countKey);
        if(StringUtils.isEmpty(count)){
            //没有发送过验证码
            jedis.setex(countKey, 60*60*24, "1");
            //将随机生成的验证码存放在codeKey
            jedis.setex(codeKey, 120, code+"");
        }else if (Integer.parseInt(count) > 3) {
            System.out.println("每日只有3次");
            return "每日只有3次，超出3次";
        }
        //将随机生成的验证码存放在codeKey
        jedis.setex(codeKey, 120, code+"");
        jedis.incr(countKey);
        jedis.close();
        System.out.println("验证码发送成功");
        return "ok";
    }

    @PostMapping(value = "/checkVal",produces = "text/html;charset=utf-8")
    public String check(String phone,String code){
        Jedis jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        Date date = new Date();
        String dates = dateFormat.format(date);

        String codeKey =phone+":"+dates+":code";

        //获取指定的电话号码发送的验证码次数

        String codeValue = jedis.get(codeKey);
        jedis.close();
        if(StringUtils.isEmpty(codeValue)){
            System.out.println("验证码未发送或失效");
            return "验证码未发送或失效";
        }else if(!codeValue.equals(code)){
            System.out.println("验证码错误");
            return "验证码错误";
        }
        System.out.println("验证成功");
        return "ok";
    }

    @GetMapping("/toPhonePage")
    public String toPhonePage(){
        return "phone";
    }
}
