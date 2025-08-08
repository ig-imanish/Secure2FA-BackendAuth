package com.bristoHQ.securetotp.repositories.analytics;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.bristoHQ.securetotp.models.analytics.PageView;

@Repository
public interface PageViewRepository extends MongoRepository<PageView, String> {

    List<PageView> findByTimestampBetween(Date startDate, Date endDate);

    // Use derived query methods instead of @Query for count operations
    Long countByTimestampAfter(Date date);

    Long countByTimestampBetween(Date startDate, Date endDate);

    List<PageView> findByUserId(String userId);

    Long countByPageAndTimestampBetween(String page, Date startDate, Date endDate);
}
