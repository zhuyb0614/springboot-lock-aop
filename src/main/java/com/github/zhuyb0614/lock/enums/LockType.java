package com.github.zhuyb0614.lock.enums;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/9 2:40 下午
 */
public enum LockType {
    /**
     * Redis+Lua
     */
    REDIS,
    /**
     * Redission tryLockPerMills 无效
     */
    REDISSON
}
