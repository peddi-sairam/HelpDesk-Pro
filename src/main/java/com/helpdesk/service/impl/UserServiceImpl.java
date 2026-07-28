package com.helpdesk.service.impl;

import com.helpdesk.entity.User;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email address is already in use");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);

        Optional<User> existing = userRepository.findByEmail(userDetails.getEmail());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new BadRequestException("Email address is already in use by another user");
        }

        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setActive(userDetails.isActive());
        
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        // Department can be null for customers or admins
        user.setDepartment(userDetails.getDepartment());

        if (userDetails.getPassword() != null && !userDetails.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));
    }

    @Override
    public List<User> getUsersByCompany(Long companyId) {
        return userRepository.findByCompanyId(companyId);
    }

    @Override
    public Page<User> getUsersByCompany(Long companyId, String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findByCompanyId(companyId, pageable);
        }
        return userRepository.searchUsersByCompany(companyId, search, pageable);
    }

    @Override
    public Page<User> getAllUsers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchAllUsers(search, pageable);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void changeUserStatus(Long id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password does not match");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
