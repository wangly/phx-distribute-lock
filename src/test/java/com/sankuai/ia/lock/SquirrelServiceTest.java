// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock;

import com.sankuai.ia.lock.param.ReentrantLockParam;
import com.sankuai.ia.lock.service.SquirrelService;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/3 上午11:58
 **/
public class SquirrelServiceTest extends BaseTest {

    @Test
    public void test() {
        ReentrantLockParam param = new ReentrantLockParam();
        param.setKey("1000000");
        param.setTraceId("1");
        Consumer<String> consumer = (String arg) -> System.out.println(arg);

        SquirrelService.processWithReenLock(param, "test", consumer);

        Function<Object[], String> function = (Object[] args) -> {
            System.out.println(args);
            return "success";
        };

        String result =  SquirrelService.processWithReenLock(param, new Object[]{param}, function);
        assert result.equals("success");
    }

}