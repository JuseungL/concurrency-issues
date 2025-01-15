package com.concurrency.stock.service;

import com.concurrency.stock.domain.Stock;
import com.concurrency.stock.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptimisticLockStockService {

    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        // 재고 감소
        stock.decrease(quantity);

        // 갱신된 값을 저장 - 더티체킹
    }
}
