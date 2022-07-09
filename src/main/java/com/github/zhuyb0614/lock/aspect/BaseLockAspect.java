package com.github.zhuyb0614.lock.aspect;

import com.github.zhuyb0614.lock.LockProperties;
import com.github.zhuyb0614.lock.anno.Lock;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/9 11:30 上午
 */
public abstract class BaseLockAspect implements MethodInterceptor {

    protected LockProperties lockProperties;

    public BaseLockAspect(LockProperties lockProperties) {
        this.lockProperties = lockProperties;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        Lock lock = method.getAnnotation(Lock.class);
        if (lock != null) {
            return doLock(methodInvocation, method, lock);
        } else {
            return methodInvocation.proceed();
        }
    }

    protected abstract Object doLock(MethodInvocation methodInvocation, Method method, Lock lock) throws Throwable;

    protected String buildLockKey(Method method, Lock lock, Object[] args) {
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        String[] parameterNames = new DefaultParameterNameDiscoverer().getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                standardEvaluationContext.setVariable(parameterNames[i], args);
            }
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(lock.lockKey());
        String lockKey = (String) exp.getValue(standardEvaluationContext);
        if (StringUtils.isEmpty(lockProperties.getGlobalLockKeyPrefix())) {
            lockKey = lockProperties.getGlobalLockKeyPrefix() + lockKey;
        }
        return lockKey;
    }

    protected int getWaitTimeMills(Lock lock) {
        return lock.waitTimeMills() == 0 ? lockProperties.getGlobalWaitMills() : lock.waitTimeMills();
    }

    protected int getLeaseTimeMills(Lock lock) {
        return lock.leaseTimeMills() == 0 ? lockProperties.getGlobalLeaseMills() : lock.leaseTimeMills();
    }

    protected String getErrorMessage(Lock lock) {
        return lock.errorMessage().length() > 0 ? lock.errorMessage() : lockProperties.getGlobalErrorMessage();
    }
}
