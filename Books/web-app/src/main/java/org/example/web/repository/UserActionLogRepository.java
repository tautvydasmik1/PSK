package org.example.web.repository;

import org.example.web.model.UserActionLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, String> {

    // Find all actions ordered by timestamp (most recent first)
    List<UserActionLog> findAllByOrderByTimestampDesc();

    // Find actions by type
    @Query("SELECT l FROM UserActionLog l WHERE l.actionType = :actionType ORDER BY l.timestamp DESC")
    List<UserActionLog> findByActionTypeOrderByTimestampDesc(@Param("actionType") String actionType);


}
