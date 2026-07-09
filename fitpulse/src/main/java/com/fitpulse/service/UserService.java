package com.fitpulse.service;

import com.fitpulse.model.dto.RegisterRequest;
import com.fitpulse.model.entity.User;

import java.util.List;

public interface UserService {
    void register(RegisterRequest request);

    User getCurrentUser();

    List<User> getAllMembers();
}
