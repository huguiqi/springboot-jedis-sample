package com.example.demo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Demo1Application.class)
public class RedisAsyncTest {

    @Autowired
    private JedisPool pool;

    @Test
    public void testTransaction(){
        Jedis jedis = pool.getResource();

        //开启事务
        Transaction tx = jedis.multi();

        try {
            for (int i = 0; i < 10; i++) {
                tx.set("transaction_key_"+ i,"value_fuck_"+i);
            }

            int num = 9/0;
            tx.set("transaction_key_error","value_error_");
        } catch (Exception e) {
//            tx.discard();
            //回滚就会退出事务
            e.printStackTrace();
        }
        //执行报错
        List<Object> list =  tx.exec();
        list.stream().forEach(str-> {
            System.out.println("返回值:"+str);
        });
    }

    @Test
    public void testPiplineTransaction(){
        Pipeline pipeline = pool.getResource().pipelined();
        pipeline.multi();

        try {
            for (int i = 0; i < 10; i++) {
                pipeline.set("pipeline_key_"+ i,"value_pipeline_"+i);
            }
            int num = 9/0;
            pipeline.set("pipeline_key_error","value_error_");
        } catch (Exception e) {
//            pipeline.discard();
            //回滚就会退出事务
            e.printStackTrace();
        }
        if (pipeline.isInMulti()){
            //执行报错
            pipeline.exec();
            List<Object> list = pipeline.syncAndReturnAll();
            list.stream().forEach(str-> {
                System.out.println("返回值:"+str);
            });
        }

        Jedis jedis = pool.getResource();
        String val = jedis.get("pipeline_key_9");
        Assert.assertEquals("value_pipeline_9",val);

    }


    @Test
    public void testPiplineTransactionWithCancel(){
        Pipeline pipeline = pool.getResource().pipelined();
        pipeline.multi();

        try {
            for (int i = 0; i < 10; i++) {
                pipeline.set("pipeline_cancel_key_"+ i,"value_pipeline_"+i);
            }
            int num = 9/0;
            pipeline.set("pipeline_cancel_key_error","value_error_");
        } catch (Exception e) {
            pipeline.discard();
            //回滚就会退出事务
            e.printStackTrace();
        }
        if (pipeline.isInMulti()){
            //执行报错
            pipeline.exec();
            List<Object> list = pipeline.syncAndReturnAll();
            list.stream().forEach(str-> {
                System.out.println("返回值:"+str);
            });
        }

        Jedis jedis = pool.getResource();
        String val = jedis.get("pipeline_cancel_key_9");
        Assert.assertNull("应该是空的!!",val);

    }
}
