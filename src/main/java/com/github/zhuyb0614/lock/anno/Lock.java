package com.github.zhuyb0614.lock.anno;


import java.lang.annotation.*;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/7 6:03 下午
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Lock {
    /**
     * 加锁key 可使用SPEL 默认为全方法名缩写
     *
     * @return
     */
    String lockKey() default "";

    /**
     * 获取不到锁时的错误信息
     *
     * @return
     */
    String errorMessage() default "";

    /**
     * 是否等待锁
     *
     * @return
     */
    boolean waitLock() default false;

    /**
     * 等待锁时长
     *
     * @return
     */
    int waitTimeMills() default 0;

    /**
     * 自动释放锁时长
     *
     * @return
     */
    int leaseTimeMills() default 0;

    /**
     * 等待时每次尝试获取锁的间隔
     *
     * @return
     */
    int tryLockPerMills() default 0;

}
