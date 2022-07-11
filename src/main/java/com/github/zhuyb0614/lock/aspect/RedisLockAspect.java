package com.github.zhuyb0614.lock.aspect;

import com.github.zhuyb0614.lock.LockProperties;
import com.github.zhuyb0614.lock.anno.Lock;
import com.github.zhuyb0614.lock.exception.LockException;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/7 6:08 下午
 */
@Slf4j
public class RedisLockAspect extends BaseLockAspect {

    public static final DefaultRedisScript<String> UNLOCK_REDIS_SCRIPT = new DefaultRedisScript<>();
    public static final StringRedisSerializer STRING_REDIS_SERIALIZER = new StringRedisSerializer();
    private static final String UNLOCK_LUA = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final Long RELEASE_SUCCESS = 1L;

    static {
        UNLOCK_REDIS_SCRIPT.setResultType(String.class);
        UNLOCK_REDIS_SCRIPT.setScriptText(UNLOCK_LUA);
    }

    private StringRedisTemplate stringRedisTemplate;

    public RedisLockAspect(LockProperties lockProperties, StringRedisTemplate stringRedisTemplate) {
        super(lockProperties);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private boolean unlock(String key, String value) {
        Object result = stringRedisTemplate.execute(UNLOCK_REDIS_SCRIPT, STRING_REDIS_SERIALIZER, STRING_REDIS_SERIALIZER, Collections.singletonList(key), value);
        //返回最终结果
        boolean isSuccess = RELEASE_SUCCESS.equals(result);
        if (isSuccess) {
            log.debug("unlock key {} value {} success", key, value);
        } else {
            log.warn("unlock key {} value {} fail result {}", key, value, result);
        }
        return isSuccess;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        Lock lock = method.getAnnotation(Lock.class);
        if (lock != null) {
            return doLock(methodInvocation, method, lock);
        }
        return methodInvocation.proceed();
    }

    @Override
    protected Object doLock(MethodInvocation methodInvocation, Method method, Lock lock) throws Throwable {
        Object result;
        //参数
        Object[] args = methodInvocation.getArguments();
        //定义传入方法的上下文参数并解析最终的key
        String lockKey = buildLockKey(method, lock, args);
        String uuid = UUID.randomUUID().toString();
        Boolean setSuccess = Boolean.FALSE;
        try {
            int leaseTimeMills = getLeaseTimeMills(lock);
            if (lock.waitLock()) {
                long startTimeMills = System.currentTimeMillis();
                int waitTimeMills = getWaitTimeMills(lock);
                while (true) {
                    setSuccess = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid, Duration.of(leaseTimeMills, ChronoUnit.MILLIS));
                    if (Boolean.TRUE.equals(setSuccess)) {
                        result = methodInvocation.proceed();
                        break;
                    } else {
                        if (System.currentTimeMillis() - startTimeMills > waitTimeMills) {
                            throw new LockException(getErrorMessage(lock));
                        }
                        Thread.sleep(lock.tryLockPerMills() == 0 ? lockProperties.getTryLockPerMills() : lock.tryLockPerMills());
                    }
                }
            } else {
                setSuccess = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, uuid, Duration.of(leaseTimeMills, ChronoUnit.MILLIS));
                if (Boolean.TRUE.equals(setSuccess)) {
                    result = methodInvocation.proceed();
                } else {
                    throw new LockException(getErrorMessage(lock));
                }
            }
        } catch (InterruptedException e) {
            log.error("lock {} error", lockKey, e);
            throw new LockException(getErrorMessage(lock));
        } finally {
            if (Boolean.TRUE.equals(setSuccess)) {
                unlock(lockKey, uuid);
            }
        }
        return result;
    }

}
