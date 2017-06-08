// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.service;

import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;
import com.dianping.squirrel.client.impl.redis.spring.RedisClientBeanFactory;
import com.dianping.squirrel.common.exception.StoreException;
import com.sankuai.ia.phx.utils.exception.APIRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 调squirrel接口
 * 设置重试机制,重试失败则忽略,避免squirrel影响功能
 *
 * @author wangliyue
 * @version 1.0
 * @created 17/6/5 下午7:44
 **/
@Service
public class SquirrelClient {

    private static Logger logger = LoggerFactory.getLogger(SquirrelClient.class);

    private RedisStoreClient storeClient;
    private static final String ROUTER_TYPE = "master-only";
    private static final String CLUSTER_NAME_PROPERTY = "phx.lock.cache.cluster.name";
    private static final int RETRY_TIME = 3;

    @PostConstruct
    public void init() {
        RedisClientBeanFactory factory = new RedisClientBeanFactory();
        factory.setClusterName(System.getProperty(CLUSTER_NAME_PROPERTY));
        factory.setReadTimeout(100);
        factory.setRouterType(ROUTER_TYPE);
        factory.setPoolMaxIdle(16);
        factory.setPoolMaxTotal(32);
        factory.setPoolMinIdle(3);
        factory.setPoolWaitMillis(500);
        try {
            storeClient = factory.getObject();
        } catch (Exception e) {
            throw new APIRuntimeException(e);
        }
    }

    //重试机制
    public <T> T get(StoreKey storeKey) {
        int tryTime = 1;
        do {
            try {
                return storeClient.get(storeKey);
            } catch (StoreException e) {
                logger.error("SquirrelClient.get StoreException error->", e);
            }
        } while (tryTime++ < RETRY_TIME);
        return null;
    }

    public <T> boolean setnx(StoreKey storeKey, T t, Integer expireTime) {
        int tryTime = 1;
        do {
            try {
                return storeClient.setnx(storeKey, t, expireTime);
            } catch (StoreException e) {
                logger.error("SquirrelClient.setnx StoreException error->", e);
            }
        } while (tryTime++ < RETRY_TIME);
        return true;
    }

    public <T> boolean compareAndSet(StoreKey storeKey, T oldObject, T newObject, Integer expireTime) {
        int tryTime = 1;
        do {
            try {
                return storeClient.compareAndSet(storeKey, oldObject, newObject, expireTime);
            } catch (StoreException e) {
                logger.error("SquirrelClient.compareAndSet StoreException error->", e);
            }
        } while (tryTime++ < RETRY_TIME);
        return true;

    }

    public <T> boolean compareAndDelete(StoreKey storeKey, T oldObject) {
        int tryTime = 1;
        do {
            try {
                return storeClient.compareAndDelete(storeKey, oldObject);
            } catch (StoreException e) {
                logger.error("SquirrelClient.compareAndDelete StoreException error->", e);
            }
        } while (tryTime++ < RETRY_TIME);
        return true;
    }

}