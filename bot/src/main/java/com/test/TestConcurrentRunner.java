package com.test;

import com.test.tests.TestConcurrent;

public class TestConcurrentRunner {

    public static void main(String[] args) {
        new TestConcurrent(10).run();
    }

}
