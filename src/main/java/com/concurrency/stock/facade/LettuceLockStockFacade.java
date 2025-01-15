package com.concurrency.stock.facade;

import com.concurrency.stock.repository.RedisLockRepository;
import com.concurrency.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {
    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }

        try {
            stockService.decreaseNamedLock(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}
