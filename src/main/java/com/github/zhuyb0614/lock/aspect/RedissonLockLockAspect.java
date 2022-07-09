package com.github.zhuyb0614.lock.aspect;

import com.github.zhuyb0614.lock.LockProperties;
import com.github.zhuyb0614.lock.anno.Lock;
import com.github.zhuyb0614.lock.exception.LockException;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/7 6:08 下午
 */
@Slf4j
public class RedissonLockLockAspect extends BaseLockAspect {

    private RedissonClient redissonClient;

    public RedissonLockLockAspect(LockProperties lockProperties, RedissonClient redissonClient) {
        super(lockProperties);
        this.redissonClient = redissonClient;
    }

    @Override
    protected Object doLock(MethodInvocation methodInvocation, Method method, Lock lock) throws Throwable {
        Object result;
        //参数
        Object[] args = methodInvocation.getArguments();
        String lockKey = buildLockKey(method, lock, args);
        RLock rLock = null;
        try {
            rLock = redissonClient.getLock(lockKey);
            if (rLock == null) {
                throw new LockException(getErrorMessage(lock));
            }
            if (lock.waitLock()) {
                if (rLock.tryLock(getWaitTimeMills(lock), getLeaseTimeMills(lock), TimeUnit.MILLISECONDS)) {
                    result = methodInvocation.proceed();
                } else {
                    throw new LockException(getErrorMessage(lock));
                }
            } else {
                if (rLock.tryLock()) {
                    result = methodInvocation.proceed();
                } else {
                    throw new LockException(lock.errorMessage());
                }
            }
        } catch (InterruptedException e) {
            log.error("lock {} error", lockKey, e);
            Thread.currentThread().interrupt();
            throw new LockException(lock.errorMessage());
        } finally {
            if (rLock != null && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
        return result;
    }

}
