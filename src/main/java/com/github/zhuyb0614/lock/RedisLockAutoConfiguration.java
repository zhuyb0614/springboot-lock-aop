package com.github.zhuyb0614.lock;

import com.github.zhuyb0614.lock.anno.Lock;
import com.github.zhuyb0614.lock.aspect.RedisLockAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnProperty(value = "yb.lock-aop.open-switch", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(RedisAutoConfiguration.class)
@Slf4j
@EnableConfigurationProperties(LockProperties.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisLockAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnProperty(name = "yb.lock-aop.lock-type", havingValue = "redis")
    @SuppressWarnings("all")
    public Advisor redisPointcutAdvisor(StringRedisTemplate stringRedisTemplate, LockProperties lockProperties) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(new AnnotationMatchingPointcut(null, Lock.class));
        advisor.setAdvice(new RedisLockAspect(lockProperties, stringRedisTemplate));
        return advisor;
    }
}
