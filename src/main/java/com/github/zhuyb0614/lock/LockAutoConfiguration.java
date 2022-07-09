package com.github.zhuyb0614.lock;

import com.github.zhuyb0614.lock.anno.Lock;
import com.github.zhuyb0614.lock.aspect.RedisLockAspect;
import com.github.zhuyb0614.lock.aspect.RedissonLockLockAspect;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/9 2:12 下午
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "lock-aop.open-switch", havingValue = "on", matchIfMissing = true)
@Slf4j
@EnableConfigurationProperties(LockProperties.class)
public class LockAutoConfiguration {
    @Bean
    @SuppressWarnings("all")
    public Advisor redssionPointcutAdvisor(RedissonClient redissonClient, StringRedisTemplate stringRedisTemplate, LockProperties lockProperties) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, Lock.class);
        advisor.setPointcut(pointcut);
        MethodInterceptor interceptor;
        switch (lockProperties.getLockType()) {
            case redis:
                interceptor = new RedisLockAspect(lockProperties, stringRedisTemplate);
                break;
            case redission:
                interceptor = new RedissonLockLockAspect(lockProperties, redissonClient);
                break;
            default:
                throw new IllegalArgumentException("未知的锁类型");
        }
        advisor.setAdvice(interceptor);
        return advisor;
    }
}
