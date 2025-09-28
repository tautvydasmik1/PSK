package org.example.web.service;

import org.example.web.model.UserActionLog;
import org.example.web.repository.UserActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserActionLogService {

    @Autowired
    private UserActionLogRepository userActionLogRepository;

    /**
     * Log a user action
     */
    @Transactional
    public UserActionLog logAction(UserActionLog action) {
        return userActionLogRepository.save(action);
    }

    /**
     * Get all user actions (most recent first)
     */
    public List<UserActionLog> getAllActions() {
        return userActionLogRepository.findAllByOrderByTimestampDesc();
    }

    /**
     * Get actions by type
     */
    public List<UserActionLog> getActionsByType(String actionType) {
        return userActionLogRepository.findByActionTypeOrderByTimestampDesc(actionType);
    }


}
