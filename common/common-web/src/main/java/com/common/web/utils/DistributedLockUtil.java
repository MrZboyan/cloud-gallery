package com.common.web.utils;

import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DistributedLockUtil {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 分布式锁的统一执行方法
     *
     * @param key  锁 key
     * @param task 要执行的任务
     */
    public <T> T execute(String key, LockTask<T> task) {
        RLock lock = redissonClient.getLock(key);
        try {
            // 未获取立即返回
            boolean gotLock = lock.tryLock(0, 3, TimeUnit.SECONDS);
            if (!gotLock) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
            }
            return task.execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    public interface LockTask<T> {
        T execute();
    }
}
