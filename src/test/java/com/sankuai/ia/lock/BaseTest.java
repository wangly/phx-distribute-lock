// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import org.junit.BeforeClass;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/16 下午10:44
 **/
public class BaseTest {

    @BeforeClass
    public static void init() {
        System.setProperty("phx.lock.cache.cluster.name", "redis-hotel-phx_dev");
    }
}