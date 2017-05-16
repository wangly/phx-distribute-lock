// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.utils;

import com.meituan.mtrace.TraceParam;
import com.meituan.mtrace.Tracer;
import org.apache.commons.lang.StringUtils;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/2 下午6:19
 **/
public class TraceIdUtils {

    public static String id() {
        if (StringUtils.isBlank(Tracer.id())) {
            //id不存在,初始化Tracer,以生成新id
            Tracer.serverRecv(new TraceParam("init"));
        }
        return Tracer.id();
    }

}