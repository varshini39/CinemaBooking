package com.cinema.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;

public class RedisUtil {
	
	static Logger logger = Logger.getLogger(RedisUtil.class.getName());
	private static final String REDIS_HOST = "127.0.0.1";
	private static final int REDIS_PORT = 6379;
	private static Jedis redisPool = null;
	
	/* This method is used for establishing redis connection */
	public static Jedis getRedisConnection()
	{
		try {
			if(redisPool==null) {
				redisPool = new Jedis(REDIS_HOST, REDIS_PORT);
				System.out.println("Checking redis: " + redisPool.ping());
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, "Error occurred in getRedisConnection");
			logger.log(Level.SEVERE, "Exception", e);
		}
		return redisPool;
	}
	
	/**
	* This method is used to store value to redis
	* @param key : For identification
	* @param value : For storing the value */
	public static void storeValueToRedis(String key, Object value)
	{
		try
		{
			redisPool = getRedisConnection();
			redisPool.set(key, String.valueOf(value));
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Error occurred in storeValueToRedis");
			logger.log(Level.SEVERE, "Exception", e);
		}
	}
	
	/**
	* This method is used to store values in redis with expire time
	* @param key : For identification
	* @param value : For storing the value
	* @param extime : Expire time in seconds */	
	public static void putInRedisWithExpireTime(String key, Object value , int extime)
	{
		try
		{
			redisPool = getRedisConnection();
			storeValueToRedis(key, value);
			redisPool.expire(key, extime);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Error occurred in putInRedisWithExpireTime");
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * This method is used to get value from redis
	 * @param key : For identification */
	public static Object getValueFromRedis(String key)
	{
		Object obj=null;
		try
		{
			redisPool = getRedisConnection();
			if(hasKeyInRedis(key)) {
				obj = redisPool.get(key);
			} else {
				throw new Exception("Key does not exist");
			}
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Error occurred in getValueFromRedis:");
			logger.log(Level.SEVERE, "Exception", e);
		}
		return obj;
	}

	/**
	 * This method is used to delete value from redis
	 * @param key : For identification */
	public static void deleteKeyFromRedis(String key)
	{
		try
		{
			redisPool = getRedisConnection();
			if(hasKeyInRedis(key)) {
				logger.log(Level.INFO, "Deleting key {0} in redis", key);
				redisPool.del(key);
			}
		}
		catch (Exception e) {
			logger.log(Level.SEVERE, "Error occurred in deleteKeyFromRedis:");
			logger.log(Level.SEVERE, "Exception", e);
		}
	}

	/**
	 * This method is used to check if key is available in redis
	 * @param key : For identification */
	public static boolean hasKeyInRedis(String key)
	{
		try
		{
			redisPool = getRedisConnection();
			return redisPool.exists(key);
		}
		catch (Exception e)
		{
			logger.log(Level.SEVERE, "Error occurred in hasKeyInRedis:");
			logger.log(Level.SEVERE, "Exception", e);
		}
		return false;
	}
	
}
