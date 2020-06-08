package com.jesper.seckill.redis;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jesper.seckill.bean.OrderInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * redis服务
 */
@Service
public class RedisService {

    @Autowired
    RedisUtils redisUtils;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 从redis连接池获取redis实例
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        //对key增加前缀，即可用于分类，也避免key重复
        String realKey = prefix.getPrefix() + key;
        Object o =  redisUtils.get(realKey);
        T t = objectToBean(o, clazz);
        return t;

    }

    /**
     * 存储对象
     */
    public <T> Boolean set(KeyPrefix prefix, String key, Object value) {
        /*String str = beanToString(value);
        if (str == null || str.length() <= 0) {
            return false;
        }*/
        String realKey = prefix.getPrefix() + key;
        int seconds = prefix.expireSeconds();//获取过期时间
        return redisUtils.set(realKey, value, seconds);
    }

    /**
     * 删除
     */
    public void delete(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        redisUtils.del(realKey);
    }

    /**
     * 判断key是否存在
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        return redisUtils.hasKey(realKey);
    }

    /**
     * 增加值
     * Redis Incr 命令将 key 中储存的数字值增一。    如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        return redisUtils.incr(realKey, 1);
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        return redisUtils.decr(realKey, 1);
    }


    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return String.valueOf(value);
        } else if (clazz == long.class || clazz == Long.class) {
            return String.valueOf(value);
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }

    }

    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }
    
    private <T> T objectToBean(Object str, Class<T> clazz) {
    	if(StringUtils.isEmpty(str)) {
    		return null;
    	}
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str.toString());
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str.toString());
        } else if (clazz == String.class) {
            return (T) str;
        } else {
            return objectMapper.convertValue(str, clazz);
        }
    }


}
