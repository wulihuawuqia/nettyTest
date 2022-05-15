package com.example;

import com.example.utils.BytesUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.LongAdder;

/**
 * program nettyTest
 * <p>
 * description
 *
 * @author wuqia
 * @date 2022-05-14 22:23
 **/
@Slf4j
public class CharLengthTest {

    @Test
    public void testLen() {
        log.error("12遛弯呢 byte len = {}", "12遛弯呢".getBytes(StandardCharsets.UTF_8).length);

        log.error("123 byte len = {}", "123".getBytes(StandardCharsets.UTF_8).length);

        Integer integer = 100;
        log.error("1 byte len = {}", (new byte[]{1}).length );
        log.error("int len = {}", (BytesUtils.intToBytes(1)).length );
        log.error("long len = {}", (BytesUtils.longToBytes(1)).length );
    }

    @Test
    public void LongAddr() {
        LongAdder longAdder = new LongAdder();
        log.error("longAdder init = {}", longAdder);
    }
}
