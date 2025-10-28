package org.cheplay;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Explicitly point to the application's main configuration class which is in package com.cheplay
@SpringBootTest(classes = com.cheplay.ChePlayApplication.class)
class ChePlayApplicationTests {

    @Test
    void contextLoads() {
    }

}
