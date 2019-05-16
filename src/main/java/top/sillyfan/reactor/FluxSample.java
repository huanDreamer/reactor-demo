package top.sillyfan.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by zhanghuan022@ke.com on 07/05/2019.
 */
public class FluxSample {

    @Test
    public void createFlux() throws InterruptedException {

        // 创建
        Flux.just(1, 2, 3).log().subscribe();

        // 定时顺序生成
        Flux.interval(Duration.ofSeconds(1)).subscribe(System.out::println);
        Thread.sleep(5000); // 延时结束

        // 创建一个序列
        Flux.create(sink -> {
            sink.next(1);
            sink.next(2);
            sink.complete();
        }).subscribe(System.out::println);

        // generate 中的 SynchronousSink 只能调用一次.next方法
        Flux.generate(sink -> {
            sink.next("once");
            sink.complete();
        }).subscribe(System.out::println);

        // 批量生成，并且一次性打印出结果，也可以在subscribe中依次打印
        Flux.generate(ArrayList::new, (list, sink) -> {
            int value = list.size() + 1;
            sink.next(value);

            list.add(value);

            if (list.size() == 5) {
                sink.complete();
            }
            return list;
        }, System.out::println).subscribe();
    }

    @Test
    public void testWindow() {

        Flux.range(1, 100).window(20).subscribe(System.out::println);

        Flux.interval(Duration.ofSeconds(1)).window(Duration.ofMillis(1001)).take(2).toStream().forEach(System.out::println);
    }

    @Test
    public void testZip() {

        // zipWith 将当前流的元素和另一个流按照一对一进行合并
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"))
                .subscribe(System.out::println);

        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);
    }


    @Test
    public void testStream() {

        // filter
        Flux.just(1, 2, 3).filter(i -> i > 2).subscribe(System.out::println);

        // 求和
        Flux.just(1, 2, 3).subscribeOn(Schedulers.parallel(), false).filter(i -> i != 2).reduce((i, j) -> i + j).log().subscribe();

        // 转换成stream，求和
        Mono.just(Flux.just(1, 2, 3).toStream().mapToInt(Integer::intValue).sum()).subscribe(System.out::println);


        // 进行分组，然后统计
        Flux.just(1, 2, 3, 4, 2, 2, 1, 2, 4, 2, 3).groupBy(Function.identity())
                .flatMap(group -> Mono.just(group.key()).zipWith(group.count()))
                .map(g -> "num: " + g.getT1() + "; count: " + g.getT2())
                .subscribe(System.out::println);
    }

    @Test
    public void testCombine() {
        Flux.combineLatest(
                Arrays::toString,
                Flux.interval(Duration.ofMillis(100)).take(5),
                Flux.interval(Duration.ofMillis(50)).take(5)
        ).toStream().forEach(System.out::println);
    }
}
