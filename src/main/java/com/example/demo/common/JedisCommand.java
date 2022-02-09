package com.example.demo.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

@Component
@Slf4j
public class JedisCommand {

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    private Jedis getJedis() {
        JedisConnection  jedisConnection = (JedisConnection)jedisConnectionFactory.getConnection();
        return jedisConnection.getNativeConnection();
    }

    public boolean setnx(String key, String val) {
        Jedis jedis = this.getJedis();
        try {
            if (jedis == null) {
                return false;
            }
            String ret = jedis.set(key, val, "NX", "PX", 1000 * 60);
            if (StringUtils.isEmpty(ret)){
                return false;
            }else
                return ret.equalsIgnoreCase("ok");
        } catch (Exception ex) {
            log.error("setnx error!!,key={},val={}",key,val,ex);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    public int delnx(String key, String val) {
        Jedis jedis = this.getJedis();
        try {
            if (jedis == null) {
                return 0;
            }
        //if redis.call('get','orderkey')=='1111' then return redis.call('del','orderkey') else return 0 end
        StringBuilder sbScript = new StringBuilder();
        sbScript.append("if redis.call('get','").append(key).append("')").append("=='").append(val).append("'").
                append(" then ").
               append("    return redis.call('del','").append(key).append("')").
                append(" else ").
                append("    return 0").
                append(" end");

        return Integer.valueOf(jedis.eval(sbScript.toString()).toString());
    } catch (Exception ex) {
            log.error("delnx error!!,key={},value={}",key,val,ex);
    } finally {
        if (jedis != null) {
            jedis.close();
        }
   }
    return 0;
    }



}
