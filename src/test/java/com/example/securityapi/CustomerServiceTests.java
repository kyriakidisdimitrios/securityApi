//package com.example.securityapi;
//
//import com.example.securityapi.model.Customer;
//import com.example.securityapi.service.CustomerService;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//class CustomerServiceTests {
//
//    @Autowired
//    private CustomerService customerService;
//
//    @Test
//    void testCreateCustomerSuccessfully() {
//        Customer newCustomer = Customer.builder()
//                .username("ronald88")
//                .password("testPassword123")
//                .name("William")
//                .surname("Pugh")
//                .address("943 Phillips Row Suite 999\nBarkerbury, IL 25437")
//                .phoneNumber("364.208.4476")
//                .email("alyssabrown@davis.biz")
//                .dateOfBirth(LocalDate.of(1990, 5, 15))
//                .isAdmin(false)
//                .build();
//
//        customerService.saveCustomer(newCustomer);
//
//        Customer found = customerService.findByUsername("ronald88");
//        assertNotNull(found);
//        assertEquals("alyssabrown@davis.biz", found.getEmail());
//        assertFalse(found.isAdmin());
//    }
//}
