package top.sillyfan.reactor;

import org.junit.Test;
import reactor.core.publisher.Mono;

/**
 * Created by zhanghuan022@ke.com on 09/05/2019.
 */
public class MonoSample {


    @Test
    public void createMono() {

        // 创建
        Mono.create(sink -> {
            sink.success(1);
        }).subscribe(System.out::println);
    }
}
