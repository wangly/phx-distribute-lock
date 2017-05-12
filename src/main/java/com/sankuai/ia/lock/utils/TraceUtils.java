// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.utils;

import com.meituan.mtrace.IdGen;
import com.meituan.mtrace.Tracer;
import org.apache.commons.lang.StringUtils;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/2 下午6:19
 **/
public class TraceUtils {

    public static String id() {
        return StringUtils.isBlank(Tracer.id()) ? String.valueOf(IdGen.get()) : Tracer.id();
    }

}