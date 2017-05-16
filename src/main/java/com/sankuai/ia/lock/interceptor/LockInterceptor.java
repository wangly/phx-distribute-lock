// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.interceptor;

import com.sankuai.ia.lock.annotation.BatchReenLock;
import com.sankuai.ia.lock.annotation.ReenLock;
import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;
import com.sankuai.ia.lock.service.SquirrelLock;
import com.sankuai.ia.lock.utils.TraceIdUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/16 下午11:04
 **/
@Aspect
@Component
public class LockInterceptor {

    @Autowired
    private SquirrelLock squirrelLock;

    @Pointcut("@annotation(com.sankuai.ia.lock.annotation.ReenLock)")
    public void methodAnnotatedReenLock() {

    }

    @Pointcut("@annotation(com.sankuai.ia.lock.annotation.BatchReenLock)")
    public void methodAnnotatedBatchReenLock() {

    }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
    }

    @Around("methodAnnotatedReenLock() && publicMethod()")
    public Object lock(ProceedingJoinPoint jp) throws Throwable {
        //before 加锁
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        ReenLock reenLockAnnotaion = method.getAnnotation(ReenLock.class);
        //解析获取key
        String fieldKey = reenLockAnnotaion.fieldKey();
        String key = StringUtils.isBlank(fieldKey) ? null : parseKey(fieldKey, method, jp.getArgs());
        if (StringUtils.isBlank(key)) {//key不存在,则无需加锁
            return jp.proceed(jp.getArgs());
        }
        String traceId = TraceIdUtils.id();
        ReentrantLockParam lockParam = new ReentrantLockParam();
        lockParam.setCategory(reenLockAnnotaion.category());
        lockParam.setKey(key);
        lockParam.setExpireTime(reenLockAnnotaion.expireTime());
        lockParam.setTraceId(traceId);
        int value = squirrelLock.reentrantLock(lockParam);

        try {
            return jp.proceed(jp.getArgs());
        } finally {
            //after 解锁
            ReentrantUnlockParam unlockParam = new ReentrantUnlockParam();
            unlockParam.setCategory(reenLockAnnotaion.category());
            unlockParam.setKey(key);
            unlockParam.setExpireTime(reenLockAnnotaion.expireTime());
            unlockParam.setTraceId(traceId);
            unlockParam.setOldValue(value);
            squirrelLock.reentrantUnlock(unlockParam);
        }
    }

    @Around("methodAnnotatedBatchReenLock() && publicMethod()")
    public Object batchLock(ProceedingJoinPoint jp) throws Throwable {
        //before 加锁
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        BatchReenLock reenLockAnnotaion = method.getAnnotation(BatchReenLock.class);
        //解析获取key
        String fieldKey = reenLockAnnotaion.fieldKey();
        String keyListStr = StringUtils.isBlank(fieldKey) ? null : parseKey(fieldKey, method, jp.getArgs());
        if (StringUtils.isBlank(keyListStr)) {//key不存在,则无需加锁
            return jp.proceed(jp.getArgs());
        }
        List<String> keys = Arrays.asList(StringUtils.split(keyListStr, ","));
        String traceId = TraceIdUtils.id();
        boolean isEmptyKeys = CollectionUtils.isEmpty(keys);
        Map<String, Integer> valueMap = new HashMap<>(keys.size());
        if (!isEmptyKeys) {
            keys.stream().forEach(key -> {
                //加锁
                ReentrantLockParam lockParam = new ReentrantLockParam();
                lockParam.setCategory(reenLockAnnotaion.category());
                lockParam.setKey(key);
                lockParam.setExpireTime(reenLockAnnotaion.expireTime());
                lockParam.setTraceId(traceId);
                int value = squirrelLock.reentrantLock(lockParam);
                valueMap.put(key, value);
            });
        }

        try {
            return jp.proceed(jp.getArgs());
        } finally {
            //after 解锁
            if (!isEmptyKeys) {
                keys.stream().forEach(key -> {
                    //解锁
                    ReentrantUnlockParam unlockParam = new ReentrantUnlockParam();
                    unlockParam.setCategory(reenLockAnnotaion.category());
                    unlockParam.setKey(key);
                    unlockParam.setExpireTime(reenLockAnnotaion.expireTime());
                    unlockParam.setTraceId(traceId);
                    unlockParam.setOldValue(valueMap.get(key));
                    squirrelLock.reentrantUnlock(unlockParam);
                });
            }
        }
    }

    /**
     * 获取缓存的key
     * key 定义在注解上，支持SPEL表达式
     *
     * @param key
     * @param method
     * @param args
     * @return
     */
    private String parseKey(String key, Method method, Object[] args) {
        //获取被拦截方法参数名列表(使用Spring支持类库)
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paraNameArr = u.getParameterNames(method);

        //使用SPEL进行key的解析
        ExpressionParser parser = new SpelExpressionParser();
        //SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        //把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        try {
            return parser.parseExpression(key).getValue(context, String.class);
        } catch (Exception e) {
            //解析错误,即需要加锁字段可能不存在,则不处理
            e.printStackTrace();
            return null;
        }
    }

}