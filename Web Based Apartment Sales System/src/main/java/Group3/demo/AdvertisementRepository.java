package com.group3.demo.repository;

import com.group3.demo.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    // Custom query: Find active promotions (for customers)
    List<Advertisement> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate todayStart, LocalDate todayEnd);
}