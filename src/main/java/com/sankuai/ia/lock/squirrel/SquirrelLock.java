// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.squirrel;

import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.impl.redis.RedisStoreClient;
import com.dianping.squirrel.client.impl.redis.spring.RedisClientBeanFactory;
import com.sankuai.ia.lock.consts.SquirrelConsts;
import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;
import com.sankuai.ia.phx.aop.annonation.ValidationBody;
import com.sankuai.ia.phx.utils.AssertUtils;
import com.sankuai.ia.phx.utils.CompareUtils;
import com.sankuai.ia.phx.utils.exception.APIRuntimeException;
import com.sankuai.ia.phx.utils.paramvalid.IgnoreValidate;
import com.sankuai.ia.phx.utils.resp.IResponseStatusMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/12 上午10:33
 **/
public class SquirrelLock {

    private static final Logger logger = LoggerFactory.getLogger(SquirrelLock.class);

    private RedisStoreClient storeClient;
    private static final SquirrelLock instance = new SquirrelLock();//初始时创建,避免影响加锁性能
    private static final String ROUTER_TYPE = "master-only";
    private static final String CLUSTER_NAME_PROPERTY = "phx.lock.cache.cluster.name";

    public static SquirrelLock getInstance() {
        return instance;
    }

    private SquirrelLock() {
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
            e.printStackTrace();
            throw new APIRuntimeException(e);
        }
    }

    /**
     * 可重入锁
     *
     * @param param
     * @return
     */
    @ValidationBody
    public Integer reentrantLock(ReentrantLockParam param) {
        checkParam(param);

        if (logger.isDebugEnabled()) {
            logger.info("***thread name:{}, reentrantLock begin currentTime:{}", Thread.currentThread().getName(),
                    new Date(System.currentTimeMillis()));
        }
        StoreKey storeKey = new StoreKey(param.getCategory(), param.getKey());
        String traceId = param.getTraceId();
        int value = 0;
        boolean result = false;
        Map<String, Integer> map = storeClient.get(storeKey);
        if (map == null || map.size() == 0) {//未曾加锁,直接加锁
            map = new HashMap<>();
            map.put(param.getTraceId(), ++value);
            result = storeClient.setnx(storeKey, map, param.getExpireTime());
        } else if (map.size() == 1 && map.containsKey(traceId)) {//已加锁,判断是否相同traceId
            value = map.get(traceId);
            Map<String, Integer> newMap = new HashMap<>();
            newMap.put(traceId, ++value);
            result = storeClient.compareAndSet(storeKey, map, newMap, param.getExpireTime());
        }
        if (!result) {
            //本次加锁失败
            logger.warn("reentrantLock param:{}, value:{} fail!", param, value);
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.SYNC_OP_ERROR);
        } else {
            logger.info("lock storeKey:{}, traceId:{}, value:{} success!", storeKey, traceId, value);
        }
        if (logger.isDebugEnabled()) {
            logger.info("***thread name:{}, reentrantLock end currentTime:{}",Thread.currentThread().getName(),  new Date(System.currentTimeMillis()));
        }
        return value;
    }

    private void checkParam(ReentrantLockParam param) {
        AssertUtils.notNull(param.getKey(), "Key不能为空");
        AssertUtils.notNull(param.getTraceId(), "TraceId不能为空");

        if (param.getCategory() == null) {
            param.setCategory(SquirrelConsts.DEFAULT_CATEGORY);
        }
        if (CompareUtils.lessEquals(param.getExpireTime(), 0)) {
            param.setExpireTime(SquirrelConsts.DEFAULT_EXPIRE_TIME);
        }
    }

    /**
     * 释放可重入锁
     *
     * @param param
     */
    @ValidationBody
    public boolean reentrantUnlock(ReentrantUnlockParam param) {
        checkParam(param);
        if (logger.isDebugEnabled()) {
            logger.info("***thread name:{}, reentrantUnlock begin param:{}, currentTime:{}",
                    Thread.currentThread().getName(), param, new Date(System.currentTimeMillis()));
        }
        boolean result = true;
        StoreKey storeKey = new StoreKey(param.getCategory(), param.getKey());
        Map<String, Integer> map = storeClient.get(storeKey);
        String traceId = param.getTraceId();

        if (map == null || map.size() == 0) { //无需解锁,直接返回
            return result;
        } else if (map.size() != 1 || !map.containsKey(traceId) || !CompareUtils
                .equals(map.get(traceId), param.getOldValue())) {
            //非唯一锁或非当前traceId锁
            logger.info("reentrantUnlock param:{} has been lock map:{}", param, map);
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.SYNC_OP_ERROR, "解锁失败");
        }
        int value = map.get(traceId);
        value--;
        if (CompareUtils.lessEquals(value, 0)) {//当前锁数为1,解锁只需删除
            result = storeClient.compareAndDelete(storeKey, map);
        } else {
            HashMap<String, Integer> newMap = new HashMap<>();
            newMap.put(traceId, value);
            result = storeClient.compareAndSet(storeKey, map, newMap, param.getExpireTime());
        }
        if (!result) {
            logger.warn("reentrantUnlock param:{}, fail!", param);
        }
        logger.info("unlock storeKey:{}, traceId:{}, value:{}", storeKey, traceId, value);
        if (logger.isDebugEnabled()) {
            logger.info("***thread name:{}, reentrantUnlock end currentTime:{}", Thread.currentThread().getName(),
                    new Date(System.currentTimeMillis()));
        }
        return result;
    }

    private void checkParam(ReentrantUnlockParam param) {
        AssertUtils.notNull(param.getKey(), "Key不能为空");
        AssertUtils.notNull(param.getTraceId(), "TraceId不能为空");
        AssertUtils.notNull(param.getOldValue(), "OldValue不能为空");
        if (param.getCategory() == null) {
            param.setCategory(SquirrelConsts.DEFAULT_CATEGORY);
        }
        if (CompareUtils.lessEquals(param.getExpireTime(), 0)) {
            param.setExpireTime(SquirrelConsts.DEFAULT_EXPIRE_TIME);
        }
    }

}