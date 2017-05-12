// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.service;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/5/2 下午6:37
 **/
@FunctionalInterface
public interface IFunc {

    Object process(Object[] args);
}