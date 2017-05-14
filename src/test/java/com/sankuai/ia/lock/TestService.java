// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import com.sankuai.ia.lock.annotation.BatchReenLock;
import com.sankuai.ia.lock.annotation.ReenLock;
import com.sankuai.ia.lock.consts.ReenLockConsts;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/19 上午11:57
 **/
@Service
public class TestService {

    @BatchReenLock(fieldKey = "orderIds", category = ReenLockConsts.DEFAULT_CATEGORY)
    public void batchDelete(List<Long> orderIds, Long userId) {
        System.out.println("batchDelete");
    }

    @ReenLock(fieldKey = "orderId", category = ReenLockConsts.DEFAULT_CATEGORY)
    public void delete(Long orderId) {
        System.out.println("delete");
    }
}