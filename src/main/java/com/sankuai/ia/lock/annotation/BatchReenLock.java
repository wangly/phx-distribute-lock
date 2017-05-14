// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.annotation;

import com.sankuai.ia.lock.consts.ReenLockConsts;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/17 下午3:32
 **/
@Target(value = { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BatchReenLock {
    //lock的category
    String category();
    //加锁的唯一标识字段名称
    String fieldKey() default "";
    //有效时间
    int expireTime() default ReenLockConsts.DEFAULT_EXPIRE_TIME;
}