package com.example.demo.controller;


import com.example.demo.common.JedisCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


@RestController
@RequestMapping(value = "/miaosha")
@CrossOrigin(origins = "*", value = "*", allowCredentials = "true")
@Slf4j
public class MiaoShaController {

    @Autowired
    private JedisCommand jedisCommand;

    //总库存
    private long nKuCuen = 0;
    //商品key名字
    private String shangpingKey = "computer_key";
    //获取锁的超时时间 秒
    private int timeout = 30 * 1000;

    @GetMapping("/qiangdan")
    public List<String> qiangdan() {

        Long startTime = System.currentTimeMillis();
        //抢到商品的用户
        List<String> shopUsers = new ArrayList<>();

        //构造很多用户
        List<String> users = new ArrayList<>(100000);
        IntStream.range(0, 100000).parallel().forEach(b -> {
            users.add("神牛-" + b);
        });

        //初始化库存
        nKuCuen = 10;

        //模拟开抢
        users.parallelStream().forEach(b -> {
            String shopUser = qiang(b);
            if (!StringUtils.isEmpty(shopUser)) {
                shopUsers.add(shopUser);
            }
        });

        System.out.println("秒杀完成使用时间:"+ (System.currentTimeMillis() - startTime) + "ms");
        return shopUsers;
    }


    /**
     * 模拟抢单动作
     *
     * @param b
     * @return
     */
    private String qiang(String b) {
        //用户开抢时间
        long startTime = System.currentTimeMillis();

        //未抢到的情况下，30秒内继续获取锁
        while ((startTime + timeout) >= System.currentTimeMillis()) {
            //商品是否剩余
            if (nKuCuen <= 0) {
                break;
            }
            if (jedisCommand.setnx(shangpingKey, b)) {
                //用户b拿到锁
                log.info("用户{}拿到锁...", b);
                try {
                    //商品是否剩余
                    if (nKuCuen <= 0) {
                        break;
                    }

                    //模拟生成订单耗时操作，方便查看：神牛-50 多次获取锁记录
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //抢购成功，商品递减，记录用户
                    nKuCuen -= 1;

                    //抢单成功跳出
                    log.info("用户{}抢单成功跳出...所剩库存：{}", b, nKuCuen);

                    return b + "抢单成功，所剩库存：" + nKuCuen;
                } finally {
                    log.info("用户{}释放锁...", b);
                    //释放锁
                    jedisCommand.delnx(shangpingKey, b);
                }
            } else {
                //用户b没拿到锁，在超时范围内继续请求锁，不需要处理
//                if (b.equals("神牛-50") || b.equals("神牛-69")) {
//                    logger.info("用户{}等待获取锁...", b);
//                }
            }
        }
        return "";
    }


}


	
	
