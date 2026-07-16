package com.agentplatform.control;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ControlPlaneApplication.class)
class ControlPlaneApplicationSmokeTest {

    @Test
    void contextStartsWithRealRuntimeClientBean() {
        // 该烟测保留真实 RuntimeClient Bean，确保 Spring 装配链路不被 mock 掩盖。
        // 控制器测试会替换这个边界，因此无法发现生产 HttpRuntimeClient 构造器回归。
    }
}
