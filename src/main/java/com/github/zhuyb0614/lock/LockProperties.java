package com.github.zhuyb0614.lock;

import com.github.zhuyb0614.lock.enums.LockType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/9 2:17 下午
 */
@Data
@ConfigurationProperties(prefix = "yb.lock-aop")
public class LockProperties {
    /**
     * 手动开关
     */
    private boolean openSwitch = true;
    /**
     * 加锁类型
     */
    private LockType lockType = LockType.REDISSON;
    /**
     * 全局等待时长
     * 为0表示不等待
     */
    private int globalWaitMills = 2000;
    /**
     * 全局lock自动释放时长
     */
    private int globalLeaseMills = 10000;
    /**
     * 等待锁时每次尝试获取锁时长
     */
    private int tryLockPerMills = 200;
    /**
     * 全局lock key的前缀
     */
    private String globalLockKeyPrefix = "lock:";
    /**
     * 全局错误信息
     */
    private String globalErrorMessage = "获取锁失败";
}
