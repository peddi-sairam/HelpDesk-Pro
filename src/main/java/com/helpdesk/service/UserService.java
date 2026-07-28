package com.helpdesk.service;

import com.helpdesk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User userDetails);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getUsersByCompany(Long companyId);
    Page<User> getUsersByCompany(Long companyId, String search, Pageable pageable);
    Page<User> getAllUsers(String search, Pageable pageable);
    void deleteUser(Long id);
    void changeUserStatus(Long id, boolean active);
    void changePassword(Long id, String oldPassword, String newPassword);
}
