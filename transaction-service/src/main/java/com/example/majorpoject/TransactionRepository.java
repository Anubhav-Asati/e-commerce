package com.example.majorpoject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionRepository extends JpaRepository<Transaction,Integer> {


    @Transactional
    @Modifying
    @Query("update Transaction t set t.transactionStatus=?2 where t.transactionStatus=?1")
    void updateTransaction(String transactionId , String transactionStatus);


}
