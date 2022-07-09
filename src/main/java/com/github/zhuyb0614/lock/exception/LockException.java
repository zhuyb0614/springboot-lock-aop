package com.github.zhuyb0614.lock.exception;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/7 6:32 下午
 */
public class LockException extends RuntimeException {
    public LockException(String message) {
        super(message);
    }
}
