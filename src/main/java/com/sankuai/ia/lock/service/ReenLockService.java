// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.service;

import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;
import com.sankuai.ia.phx.aop.annonation.ValidationBody;
import com.sankuai.ia.phx.utils.AssemblerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/2 下午6:36
 **/
@Service
public class ReenLockService {

    @Autowired
    private SquirrelLock squirrelLock;

    @ValidationBody
    public <T,R> R processWithReenLock(ReentrantLockParam lockParam, T args, Function<T,R> lockFunc) {
        //加锁
        int value = squirrelLock.reentrantLock(lockParam);

        try {
            return lockFunc.apply(args);
        } finally {
            //after 解锁
            ReentrantUnlockParam unlockParam = AssemblerUtils.assemble(lockParam, new ReentrantUnlockParam());
            unlockParam.setOldValue(value);
            squirrelLock.reentrantUnlock(unlockParam);
        }

    }

    @ValidationBody
    public <T> void processWithReenLock(ReentrantLockParam lockParam, T args, Consumer<T> lockFunc) {
        //加锁
        int value = squirrelLock.reentrantLock(lockParam);

        try {
            lockFunc.accept(args);
        } finally {
            //after 解锁
            ReentrantUnlockParam unlockParam = AssemblerUtils.assemble(lockParam, new ReentrantUnlockParam());
            unlockParam.setOldValue(value);
            squirrelLock.reentrantUnlock(unlockParam);
        }

    }
}