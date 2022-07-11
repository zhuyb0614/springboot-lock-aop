package com.github.zhuyb0614.lock;

import com.github.zhuyb0614.lock.anno.Lock;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/11 9:51 上午
 */
@Service
@Slf4j
public class TestService {

    @SneakyThrows
    @Lock(lockKey = "'test'+#id")
    public void testReturnVoidLock(Long id) {
        log.info("id {}", id);
        Thread.sleep(10000);
    }

    @SneakyThrows
    @Lock(lockKey = "'test2'+#objParam.id", waitLock = true, waitTimeMills = 3000)
    public Integer testParseIntLock(ObjParam objParam) {
        log.info("id {}", objParam.getId());
        Thread.sleep(2000);
        return Integer.parseInt(objParam.getId());
    }

    @Data
    @Accessors(chain = true)
    public static class ObjParam {
        private String id;
        private String name;
    }
}
