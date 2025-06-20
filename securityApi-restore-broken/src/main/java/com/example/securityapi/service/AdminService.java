//package com.example.securityapi.service;
//
//import com.example.securityapi.model.Admin;
//import com.example.securityapi.repository.AdminRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class AdminService {
//
//    private final AdminRepository adminRepository;
//
//    public AdminService(AdminRepository adminRepository) {
//        this.adminRepository = adminRepository;
//    }
//
//    public boolean authenticateAdmin(String username, String password) {
//        return adminRepository.findByUsernameAndPassword(username, password).isPresent();
//    }
//
//    public Optional<Admin> findByUsername(String username) {
//        return adminRepository.findByUsernameAndPassword(username, null);
//    }
//}
