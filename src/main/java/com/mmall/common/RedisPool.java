package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by tttppp606 on 2019/3/1.
 */
public class RedisPool {
    private static JedisPool pool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20")); //最大连接数
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","20"));//在jedispool中最大的idle状态(空闲的)的jedis实例的个数
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","20"));//在jedispool中最小的idle状态(空闲的)的jedis实例的个数
    //在borrow一个jedis实例的时候，是否要进行验证操作，
    // 如果赋值true,获取Jedis实例需要经过验证，只有验证是正确的实例才会被获取。
    // 默认是false，不用检查，本程序设置为true
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));
    //在return一个jedis实例的时候，是否要进行验证操作，
    //如果赋值true，则放回jedispool的jedis实例需要经过检查，若是坏连接，丢弃
    //默认是false，不用检查，本程序设置为true
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","false"));

    private static String redisIp = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));


    private static void initPool(){
        JedisPoolConfig config = new  JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        //连接耗尽的时候，通过改配置选择是否阻塞，false会抛出异常，true阻塞直到超时（new JedisPool会设定最长等待时间）。默认为true。
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }
    //为了程序刚开始就能初始化并建立jedisPool
    static {
        initPool();
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }
    //将jedis放回连接池中，jedis不会自动都回收；Mybatis会自动将连接收回到连接池
    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();
        String a = jedis.set("a", "1");
        System.out.println(a);
        returnResource(jedis);

        pool.destroy();//临时调用，销毁连接池中的所有连接
    }
}
