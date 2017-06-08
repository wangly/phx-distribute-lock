// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.service;

import com.dianping.squirrel.client.StoreKey;
import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;
import com.sankuai.ia.phx.aop.annonation.ValidationBody;
import com.sankuai.ia.phx.utils.CompareUtils;
import com.sankuai.ia.phx.utils.exception.APIRuntimeException;
import com.sankuai.ia.phx.utils.resp.IResponseStatusMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/12 上午10:33
 **/
@Service
public class ReenLockService {

    private static final Logger logger = LoggerFactory.getLogger(ReenLockService.class);

    @Autowired
    private SquirrelClient squirrelClient;

    /**
     * 可重入锁
     *
     * @param param
     * @return
     */
    @ValidationBody
    public Integer reentrantLock(ReentrantLockParam param) {
        if (logger.isDebugEnabled()) {
            logger.info("reentrantLock begin param:{}, currentTime:{}", param, new Date(System.currentTimeMillis()));
        }
        StoreKey storeKey = new StoreKey(param.getCategory(), param.getKey());
        String traceId = param.getTraceId();
        int value = 0;
        boolean result = false;
        Map<String, Integer> map = squirrelClient.get(storeKey);
        if (map == null || map.size() == 0) {//未曾加锁,直接加锁
            map = new HashMap<>();
            map.put(param.getTraceId(), ++value);
            result = squirrelClient.setnx(storeKey, map, param.getExpireTime());
        } else if (map.size() == 1 && map.containsKey(traceId)) {//已加锁,判断是否相同traceId
            value = map.get(traceId);
            Map<String, Integer> newMap = new HashMap<>();
            newMap.put(traceId, ++value);
            result = squirrelClient.compareAndSet(storeKey, map, newMap, param.getExpireTime());
        }
        if (!result) {
            //本次加锁失败
            logger.warn("reentrantLock param:{}, value:{} fail!", param, value);
            throw new APIRuntimeException(IResponseStatusMsg.APIEnum.SYNC_OP_ERROR);
        } else {
            logger.info("lock storeKey:{}, traceId:{}, value:{} success!", storeKey, traceId, value);
        }
        if (logger.isDebugEnabled()) {
            logger.info("reentrantLock end, thread name:{}, currentTime:{}", Thread.currentThread().getName(),
                    new Date(System.currentTimeMillis()));
        }
        return value;
    }

    /**
     * 释放可重入锁
     *
     * @param param
     */
    @ValidationBody
    public boolean reentrantUnlock(ReentrantUnlockParam param) {
        if (logger.isDebugEnabled()) {
            logger.info("reentrantUnlock begin param:{}, currentTime:{}", param, new Date(System.currentTimeMillis()));
        }
        boolean result = true;
        StoreKey storeKey = new StoreKey(param.getCategory(), param.getKey());
        Map<String, Integer> map = squirrelClient.get(storeKey);
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
            result = squirrelClient.compareAndDelete(storeKey, map);
        } else {
            HashMap<String, Integer> newMap = new HashMap<>();
            newMap.put(traceId, value);
            result = squirrelClient.compareAndSet(storeKey, map, newMap, param.getExpireTime());
        }
        if (!result) {
            logger.warn("reentrantUnlock param:{}, fail!", param);
        } else {
            logger.info("unlock storeKey:{}, traceId:{}, value:{}", storeKey, traceId, value);
        }
        if (logger.isDebugEnabled()) {
            logger.info("reentrantUnlock end, thread name:{}, currentTime:{}", Thread.currentThread().getName(),
                    new Date(System.currentTimeMillis()));
        }
        return result;
    }

}