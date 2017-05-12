// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import com.sankuai.ia.lock.annotation.BatchReenLock;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/19 上午11:57
 **/
@Service
public class TestService {

    @BatchReenLock(fieldKey = "orderIds")
    public void batchDelete(List<Long> orderIds, Long userId) {
        System.out.println("batchDelete");
    }
}