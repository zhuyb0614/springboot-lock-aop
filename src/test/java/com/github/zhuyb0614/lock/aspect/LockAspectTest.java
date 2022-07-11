package com.github.zhuyb0614.lock.aspect;

import com.github.zhuyb0614.lock.TestService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yunbo.zhu
 * @version 1.0
 * @date 2022/7/11 9:49 上午
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class LockAspectTest {

    @Autowired
    private TestService testService;

    @SneakyThrows
    @Test
    public void testReturnVoidLock() {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        new CountDownThread(() -> testService.testReturnVoidLock(1L), countDownLatch).start();
        new CountDownThread(() -> testService.testReturnVoidLock(1L), countDownLatch).start();
        new CountDownThread(() -> testService.testReturnVoidLock(2L), countDownLatch).start();
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    @SneakyThrows
    @Test
    public void testParseIntLock() {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        new CountDownThread(() -> testService.testParseIntLock(new TestService.ObjParam().setId("3").setName("张三")), countDownLatch).start();
        new CountDownThread(() -> testService.testParseIntLock(new TestService.ObjParam().setId("3").setName("张三")), countDownLatch).start();
        new CountDownThread(() -> testService.testParseIntLock(new TestService.ObjParam().setId("4").setName("李四")), countDownLatch).start();
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

    public static class CountDownThread extends Thread {
        private CountDownLatch countDownLatch;

        public CountDownThread(Runnable target, CountDownLatch countDownLatch) {
            super(target);
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();
            countDownLatch.countDown();
        }
    }
}