package com.concurrency.stock.service;

import com.concurrency.stock.domain.Stock;
import com.concurrency.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private PessimisticLockStockService pessimisticLockStockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void 재고감소() {
        stockService.decrease(1L, 1L);

        // 100 - 1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(99, stock.getQuantity());
    }

    @Test
    public void 동시에_100개의_요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }
    /**
     * 그 결과 Expected :0, Actual   :89 가 나왔다.
     * 이는 RaceCondition이라는 둘 이상의 스레드가 공유 데이터에 엑세스하여 동시에 변경을 하려고할 때 발생한다.
     * 즉, 하나의 스레드가 작업을 끝내고 나머지 스레드가 엑세스하여 작업을 처리해야한다.
     */


    @Test
    public void 동시에_100개의_요청_Synchronized() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseSynchronized(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }
    /**
     * 그래도 실패!Expected :0, Actual   :62
     * 이는 @Transactional로 인해 발생하는 에러이다.
     * 왜냐하면 Service레이어의 메소드를 보면 decreaeSynchronized가 하나의 트랜잭션으로
     * 트랜잭션이 시작하고 decreaseSync-()가 실행되고 트랜잭션이 종료되는데
     * decreseSync-()가 실행됐고 그 다음 트랜잭션이 종료되며 실제 DB가 업데이트되기 전에
     * 다른 스레드가 해당 메소드를 호출할 수 있는데 그렇게되면 업데이트 전에 또 값을 조회해버려서 이전과 동일한 결과가 발생한다.
     */


    @Test
    public void 동시에_100개의_요청_Synchronized_without_transactional() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseSynchronizedWithoutTransactional(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }
    /**
     * 이렇게하면 테스트에 성공한다.
     *
     * 자바의 synchronized는 하나의 프로세스 안에서만 보장된다.
     * 만약 분산 환경이라면 하나의 서버가 아닌 여러 대의 서버에서 Stock 데이터베이스에 동시에 접근하게된다.
     *
     */


    /*------------------------------------------------------__*/
    @Test
    public void 동시에_100개의_요청_pessimistic_lock() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pessimisticLockStockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }

}
