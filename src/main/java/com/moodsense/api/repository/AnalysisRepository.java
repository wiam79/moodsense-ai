package com.moodsense.api.repository;

import com.moodsense.api.model.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findAllByUserIdOrderByAnalyzedAtDesc(Long userId);
    Optional<Analysis> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndSentiment(Long userId, String sentiment);
    Optional<Analysis> findFirstByUserIdOrderByAnalyzedAtDesc(Long userId);
}
