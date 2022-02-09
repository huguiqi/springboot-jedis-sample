package com.example.demo.config;

import com.example.subscribe.Receiver;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * Created by guiqi on 2017/8/3.
 */

@Configuration
@EnableCaching
public class RedisConfig {


    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
    }


    @SuppressWarnings("rawtypes")
    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
        rcm.setDefaultExpiration(60);
        rcm.setUsePrefix(true);
        rcm.setCachePrefix(new DefaultRedisCachePrefix("HGQ_"));
        return rcm;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(jedisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }
//    @Bean
//    JedisPool jedisPool(){
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        // 设置最大10个连接
//        jedisPoolConfig.setMaxTotal(600);
//        jedisPoolConfig.setMaxIdle(300);
//        jedisPoolConfig.setTestOnBorrow(true);
//        jedisPoolConfig.setTestOnReturn(false);
//        JedisPool pool = new JedisPool(jedisPoolConfig, "10.105.141.164",16379,1000,"redis123");
//        return pool;
//    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        //RedisConnectionFactory设置host-name，port，password都正确了，就没Connection failure occurred. Restarting subscription task 这个问题。
        jedisConnectionFactory.setHostName("10.105.141.164");
        jedisConnectionFactory.setPort(16379);
        jedisConnectionFactory.setPassword("redis123");

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        // 设置最大10个连接
        jedisPoolConfig.setMaxTotal(600);
        jedisPoolConfig.setMaxIdle(300);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(false);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig);
        return jedisConnectionFactory;
    }


    @Bean
    RedisMessageListenerContainer container(JedisConnectionFactory jedisConnectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(jedisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        //注册消息监听者
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    Receiver receiver(CountDownLatch latch) {
        return new Receiver(latch);
    }

    @Bean
    CountDownLatch latch() {
        return new CountDownLatch(1);
    }


}
