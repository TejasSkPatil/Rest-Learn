package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
    List<Payment> findByStudentId(Long studentId);
    Optional<Payment> findByProviderRef(String providerRef);
    List<Payment> findByStatus(PaymentStatus status);
}
