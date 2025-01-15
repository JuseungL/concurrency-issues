package com.concurrency.stock.repository;

import com.concurrency.stock.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {
    /**
     * Thread1이 데이터베이스에 락을 걸고 데이터를 조회한다.
     * 그떄 Thread2가 조회를 시도하게되면 이미 Thread1이 점유 중이므로 잠시 대기한다.
     * Thread1의 작업이 완료되면 Thread2가 이어서 작업을 할 수 있다.
     *
     * 이때 쿼리를 보면 select s1_0.id,s1_0.product_id,s1_0.quantity from stock s1_0 where s1_0.id=? for update 와 같은데
     * 마지막에 for update 키워드가 바로 관적 쓰기 락(PESSIMISTIC_WRITE)을 나타내는 키워드이다.
     * 해당 행(Row)을 다른 트랜잭션에서 읽거나 수정하지 못하도록 잠그는 역할을 하게된다.(락 대기 상태)
     *
     * 락을 소유한 트랜잭션에서 데이터 업데이트할 수 있음.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE )
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);


    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);
}
