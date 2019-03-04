package com.mmall.util;

import com.mmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

/**
 * 当jedis和redis之间出现物理连接问题时，jedis实例会变坏。
 * 使用过的jedis实例需要手动放回池中，不像MyBatis自动回收使用过的连接。
 * JedisPoolConfig配置中如果将TEST_ON_RETURN的默认值由false改为true，将会对放回的Jedis实例校验好坏。
 * 官方规定，坏的Jedis连接要通过returnBrokenResource放回连接池，否则异常
 *
 * TEST_ON_RETURN=true，坏的Jedis实例returnResource，会报异常
 * TEST_ON_RETURN=false（默认），坏的Jedis实例returnResource，会出现什么问题？
 *
 * 解决办法：TEST_ON_RETURN=false（默认），
 * 对Jedis操作数据库的语句tyrcatch，判断是否抛出异常，有异常的Jedis.returnBrokenResource,没异常的Jedis.returnResource
 *
 * Created by tttppp606 on 2019/3/1.
 */
@Slf4j
public class RedisShardedPoolUtil {
    //设置key的有效期
    public static Long expire(String key,int exTime){
        Long result = null;
        ShardedJedis jedis = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key:{} error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    //exTime的单位是秒 setEx 返回成功是“ok”String类型
    public static String setEx(String key,String value,int exTime){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setex(key,exTime,value);
        } catch (Exception e) {
            log.error("setex key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String set(String key,String value){
        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String get(String key){
        ShardedJedis jedis = null;
        String result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key:{} error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key){
        ShardedJedis jedis = null;
        Long result = null;
        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{} error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;
        }
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        RedisShardedPoolUtil.set("test","lichuang");
        String test = RedisShardedPoolUtil.get("test");
        System.out.println(test);
        RedisShardedPoolUtil.expire("test",60);
    }
}
