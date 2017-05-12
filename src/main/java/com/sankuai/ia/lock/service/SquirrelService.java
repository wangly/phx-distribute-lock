// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.service;

import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;
import com.sankuai.ia.lock.squirrel.SquirrelLock;
import com.sankuai.ia.phx.utils.AssemblerUtils;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/2 下午6:36
 **/
public class SquirrelService {

    public static <T,R> R processWithReenLock(ReentrantLockParam lockParam, T args, Function<T,R> lockFunc) {
        SquirrelLock squirrelLock = SquirrelLock.getInstance();
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

    public static <T> void processWithReenLock(ReentrantLockParam lockParam, T args, Consumer<T> lockFunc) {
        SquirrelLock squirrelLock = SquirrelLock.getInstance();
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