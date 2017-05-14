// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import com.sankuai.ia.lock.consts.ReenLockConsts;
import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.param.ReentrantUnlockParam;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/16 下午10:39
 **/
public class MockData {

    public static ReentrantLockParam buildLockParam() {
        ReentrantLockParam lockParam = new ReentrantLockParam();
        lockParam.setCategory(ReenLockConsts.DEFAULT_CATEGORY);
        lockParam.setKey("test");
        lockParam.setExpireTime(ReenLockConsts.DEFAULT_EXPIRE_TIME);
        return lockParam;
    }

    public static ReentrantUnlockParam buildUnlockParam() {
        ReentrantUnlockParam unlockParam = new ReentrantUnlockParam();
        unlockParam.setCategory(ReenLockConsts.DEFAULT_CATEGORY);
        unlockParam.setKey("test");
        unlockParam.setExpireTime(ReenLockConsts.DEFAULT_EXPIRE_TIME);
        return unlockParam;
    }
}