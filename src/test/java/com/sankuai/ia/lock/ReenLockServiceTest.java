// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import com.sankuai.ia.lock.consts.ReenLockConsts;
import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;
import com.sankuai.ia.lock.service.ReenLockService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/16 下午10:38
 **/
public class ReenLockServiceTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(ReenLockServiceTest.class);

    private static AtomicInteger lockSleepTime = new AtomicInteger(0);
    private static AtomicInteger unlockSleepTime = new AtomicInteger(1);

    @Autowired
    private ReenLockService reenLockService;

    @Test
    public void testReenLock() {
        try {
            ExecutorService exec = Executors.newFixedThreadPool(2);

            //case1:嵌套加锁
            Callable<String> reenLockThread = () -> {
                ReentrantLockParam lockParam = new ReentrantLockParam();
                Integer traceNum = 1; /*(int) (Math.floor((Math.random() * 5) + 1));*/
                String traceId = String.valueOf(traceNum);
                lockParam.setTraceId(traceId);
                lockParam.setKey("test");
                lockParam.setCategory(ReenLockConsts.DEFAULT_CATEGORY);
                //加锁
                try {
                    Thread.sleep((lockSleepTime.getAndIncrement()) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int value = reenLockService.reentrantLock(lockParam);

                logger.info("reentrantLock value:{}", value);

                try {
                    Thread.sleep((unlockSleepTime.getAndDecrement()) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ReentrantUnlockParam unlockParam = new ReentrantUnlockParam();
                unlockParam.setKey("test");
                unlockParam.setTraceId(traceId);
                unlockParam.setCategory(ReenLockConsts.DEFAULT_CATEGORY);
                unlockParam.setOldValue(value);
                reenLockService.reentrantUnlock(unlockParam);
                return traceId;
            };

            List<Callable<String>> callableList = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                callableList.add(reenLockThread);
            }
            List<Future<String>> futures = exec.invokeAll(callableList);

            exec.shutdown();
            while (!exec.isTerminated()) {
                Thread.sleep(100);
            }
            futures.stream().forEach(future -> {
                try {
                    logger.info("traceId:{}", future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultiLock() {
        try {
            ExecutorService exec = Executors.newFixedThreadPool(2);

            //case1:串讲加锁
            Callable<String> reenLockThread = () -> {
                ReentrantLockParam lockParam = new ReentrantLockParam();
                Integer traceNum = (int) (Math.floor((Math.random() * 2) + 1));
                String traceId = String.valueOf(traceNum);
                lockParam.setTraceId(traceId);
                lockParam.setCategory(ReenLockConsts.DEFAULT_CATEGORY);
                lockParam.setKey("test");
                //加锁
                try {
                    Thread.sleep((lockSleepTime.getAndIncrement()) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int value = reenLockService.reentrantLock(lockParam);

                logger.info("reentrantLock value:{}", value);

                try {
                    Thread.sleep((unlockSleepTime.getAndDecrement()) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ReentrantUnlockParam unlockParam = new ReentrantUnlockParam();
                unlockParam.setKey("test");
                unlockParam.setTraceId(traceId);
                unlockParam.setOldValue(value);
                unlockParam.setCategory(ReenLockConsts.DEFAULT_CATEGORY);
                reenLockService.reentrantUnlock(unlockParam);
                return traceId;
            };

            List<Callable<String>> callableList = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                callableList.add(reenLockThread);
            }
            List<Future<String>> futures = exec.invokeAll(callableList);

            exec.shutdown();//禁止向exec添加新任务
            while (!exec.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.info("wait execute");
            }
            futures.stream().forEach(future -> {
                try {
                    logger.info("traceId:{}", future.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}