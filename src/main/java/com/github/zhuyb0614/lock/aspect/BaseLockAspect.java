package com.github.zhuyb0614.lock.aspect;

import com.github.zhuyb0614.lock.LockProperties;
import com.github.zhuyb0614.lock.anno.Lock;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
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

    private static final String PACKAGE_SEPARATOR = ".";
    private static final String KEY_SEPARATOR = ":";
    protected LockProperties lockProperties;

    protected BaseLockAspect(LockProperties lockProperties) {
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

    /**
     * 当前方法存在Lock注解,执行加锁逻辑
     *
     * @param methodInvocation
     * @param method
     * @param lock
     * @return
     * @throws Throwable
     */
    protected abstract Object doLock(MethodInvocation methodInvocation, Method method, Lock lock) throws Throwable;

    protected String buildLockKey(Method method, Lock lock, Object[] args) {
        String lockKey;
        if (lock.lockKey().length() == 0) {
            lockKey = classNameMethodNameLockKey(method);
        } else {
            lockKey = customLockKey(method, lock, args);
        }
        return lockKey;
    }

    private String customLockKey(Method method, Lock lock, Object[] args) {
        String lockKey;
        EvaluationContext standardEvaluationContext = new StandardEvaluationContext(args);
        String[] parameterNames = new DefaultParameterNameDiscoverer().getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                standardEvaluationContext.setVariable(parameterNames[i], args[i]);
            }
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(lock.lockKey());
        lockKey = exp.getValue(standardEvaluationContext, String.class);
        if (!StringUtils.isEmpty(lockProperties.getGlobalLockKeyPrefix())) {
            lockKey = lockProperties.getGlobalLockKeyPrefix() + lockKey;
        }
        return lockKey;
    }

    private String classNameMethodNameLockKey(Method method) {
        String lockKey;
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        String[] classNameSplit = className.split("\\.");
        StringBuilder lockKeyBuilder = new StringBuilder();
        if (!StringUtils.isEmpty(lockProperties.getGlobalLockKeyPrefix())) {
            lockKeyBuilder.append(lockProperties.getGlobalLockKeyPrefix());
            lockKeyBuilder.append(KEY_SEPARATOR);
        }
        for (int i = 0; i < classNameSplit.length - 1; i++) {
            String s = classNameSplit[i];
            lockKeyBuilder.append(s, 0, 1);
            lockKeyBuilder.append(PACKAGE_SEPARATOR);
        }
        lockKeyBuilder.append(classNameSplit[classNameSplit.length - 1]);
        lockKeyBuilder.append(KEY_SEPARATOR);
        lockKeyBuilder.append(methodName);
        lockKey = lockKeyBuilder.toString();
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
