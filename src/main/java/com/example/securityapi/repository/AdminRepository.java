//package com.example.securityapi.repository;
//
//import com.example.securityapi.model.Admin;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface AdminRepository extends JpaRepository<Admin, Long> {
//    Optional<Admin> findByUsernameAndPassword(String username, String password);
//}