package com.concurrency.stock.facade;

import com.concurrency.stock.domain.Stock;
import com.concurrency.stock.repository.LockRepository;
import com.concurrency.stock.repository.StockRepository;
import com.concurrency.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NamedLockStockFacade {
    private final LockRepository lockRepository;
    private final StockService stockService;

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decreaseNamedLock(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
