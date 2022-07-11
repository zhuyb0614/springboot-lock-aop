package com.github.zhuyb0614.lock;

import com.github.zhuyb0614.lock.anno.Lock;
import com.github.zhuyb0614.lock.aspect.RedisLockAspect;
import com.github.zhuyb0614.lock.aspect.RedissonLockAspect;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
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
@AutoConfigureAfter(value = {RedissonAutoConfiguration.class, RedisAutoConfiguration.class})
public class LockAutoConfiguration {
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnProperty(name = "lock-aop.lock-type", havingValue = "redisson", matchIfMissing = true)
    @SuppressWarnings("all")
    public Advisor redssionPointcutAdvisor(RedissonClient redissonClient, LockProperties lockProperties) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(new AnnotationMatchingPointcut(null, Lock.class));
        advisor.setAdvice(new RedissonLockAspect(lockProperties, redissonClient));
        return advisor;
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnProperty(name = "lock-aop.lock-type", havingValue = "redis")
    @SuppressWarnings("all")
    public Advisor redisPointcutAdvisor(StringRedisTemplate stringRedisTemplate, LockProperties lockProperties) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(new AnnotationMatchingPointcut(null, Lock.class));
        advisor.setAdvice(new RedisLockAspect(lockProperties, stringRedisTemplate));
        return advisor;
    }
}
