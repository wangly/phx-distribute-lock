// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/14 下午9:27
 **/
public class LockInterceptorTest extends BaseTest {

    @Autowired
    private TestService testService;

    @Test
    public void testBatchReenLock() {
        testService.batchDelete(Arrays.asList(1L, 2L), -1L);
    }

    @Test
    public void testReenLock() {
        testService.delete(1L);
    }

}