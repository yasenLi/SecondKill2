package com.atguigu.sk;

import com.atguigu.sk.util.JedisPoolUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Random;

@RestController
public class SkController {
    static String secKillScript = "local userid=KEYS[1];\r\n"
            + "local prodid=KEYS[2];\r\n"
            + "local qtkey='sk:'..prodid..\":qt\";\r\n"
            + "local usersKey='sk:'..prodid..\":usr\";\r\n"
            + "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n"
            + "if tonumber(userExists)==1 then \r\n"
            + "   return 2;\r\n"
            + "end\r\n"
            + "local num= redis.call(\"get\" ,qtkey);\r\n"
            + "if tonumber(num)<=0 then \r\n"
            + "   return 0;\r\n"
            + "else \r\n"
            + "   redis.call(\"decr\",qtkey);\r\n"
            + "   redis.call(\"sadd\",usersKey,userid);\r\n"
            + "end\r\n"
            + "return 1";

    @PostMapping(value = "/sk/doSecondKill",produces = "text/html;charset=UTF-8")
    public String doSkByLUA(Integer id){
        //随机生成用户id
        Integer usrid = (int)(10000*Math.random());
     //   Jedis jedis = new Jedis("192.168.71.128", 6379);
        JedisPool jedisPool = JedisPoolUtil.getJedisPoolInstance();
        //加载LUA脚本
        Jedis jedis = jedisPool.getResource();
        String sha1 = jedis.scriptLoad(secKillScript);
        //将LUA脚本和LUA脚本需要的参数传给redis执行：keyCount：lua脚本需要的参数数量，params：参数列表
        Object obj = jedis.evalsha(sha1, 2, usrid + "", id + "");
        jedis.close();
        // Long 强转为Integer会报错  ，Lange和Integer没有父类和子类的关系
        int result = (int)((long)obj);

        if(result==1){
            System.out.println("秒杀成功");
            return "ok";
        }else if(result==2){
            System.out.println("重复秒杀");
            return "重复秒杀";
        }else{
            System.out.println("库存不足");
            return "库存不足";
        }
    }

    //@PostMapping(value = "/sk/doSecondKill",produces = "text/html;charset=UTF-8")
    public String doSecondKill(Integer id){
        //随机生成用户id
        Integer usrid =(int)(Math.random()*10000);
        //秒杀商品的id
        Integer pid =id;
        //秒杀业务
        //拼接商品库存的key和用户列表集合的Key
        String qtKey="sk:"+pid+":qt";
        String usrsKey="sk:"+pid+":usr";
        Jedis jedis = new Jedis("192.168.71.128", 6379);
        //对库存进行监视 watch
        jedis.watch(qtKey);
        String qtStr = jedis.get(qtKey);
        if(jedis.sismember(usrsKey, usrid + "")){
            System.err.println("重复秒杀");
            return "该用户已经秒杀过，请勿重复秒杀";
        }
        if(StringUtils.isEmpty(qtStr)){
            System.err.println("秒杀尚未开始");
            return "秒杀尚未开始";
        }
        int qtNum = Integer.parseInt(qtStr);
        System.out.println("库存 = " + qtNum);
        if(qtNum<=0){
            System.err.println("库存不足");
            return "库存不足";
        }
        Transaction multi = jedis.multi();
        //减库存
        multi.decr(qtKey);
        //将用户加入到秒杀成功的列表
        multi.sadd(usrsKey, usrid+"");
        multi.exec();
        System.out.println("秒杀成功，用户id"+usrid);
        jedis.close();
        return "ok";
    }
}
