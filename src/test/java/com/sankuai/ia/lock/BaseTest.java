// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/16 下午10:44
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations ="classpath:application-context.xml")
public abstract class BaseTest extends AbstractJUnit4SpringContextTests {

    @BeforeClass
    public static void init() {
        System.setProperty("phx.lock.cache.cluster.name", "redis-hotel-phx_dev");
    }
}