// Copyright (C) 2017 Meituan
// All rights reserved
package com.sankuai.ia.lock.param;

import com.sankuai.ia.phx.utils.paramvalid.NotNull;

/**
 * @author wangliyue
 * @version 1.0
 * @created 17/4/16 下午6:57
 **/
public class ReentrantLockParam {
    @NotNull
    private String category;
    @NotNull
    private String key;
    @NotNull
    private String traceId;
    private int expireTime;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReentrantLockParam{");
        sb.append("category='").append(category).append('\'');
        sb.append(", key='").append(key).append('\'');
        sb.append(", traceId='").append(traceId).append('\'');
        sb.append(", expireTime=").append(expireTime);
        sb.append('}');
        return sb.toString();
    }
}