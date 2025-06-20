//package com.example.securityapi.controller;
//
//import com.example.securityapi.exception.BookNotFoundException;
//import com.example.securityapi.model.Admin;
//import com.example.securityapi.model.Book;
//import com.example.securityapi.model.Customer;
//import com.example.securityapi.service.AdminService;
//import com.example.securityapi.service.BookService;
//import com.example.securityapi.service.CustomerService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/admin")
//public class AdminController {
//
//    private final AdminService adminService;
//    private final BookService bookService;
//    private final CustomerService customerService;
//
//    public AdminController(AdminService adminService, BookService bookService, CustomerService customerService) {
//        this.adminService = adminService;
//        this.bookService = bookService;
//        this.customerService = customerService;
//    }
//
//    // === LOGIN PAGE ===
//    @GetMapping("/login")
//    public String loginPage() {
//        return "admin_login";
//    }
//
//    // === LOGIN FORM SUBMIT ===
//    @PostMapping("/login")
//    public String loginSubmit(@RequestParam String username,
//                              @RequestParam String password,
//                              HttpSession session,
//                              Model model) {
//        if (adminService.authenticateAdmin(username, password)) {
//            session.setAttribute("loggedInAdmin", username);
//            return "redirect:/admin/books";
//        } else {
//            model.addAttribute("error", "Invalid credentials");
//            return "admin_login";
//        }
//    }
//
//    // === LOGOUT ===
//    @GetMapping("/logout")
//    public String logout(HttpSession session) {
//        session.invalidate();
//        return "redirect:/admin/login";
//    }
//
//    // === BOOK LIST PAGE ===
//    @GetMapping("/books")
//    public String bookList(Model model, HttpSession session) {
//        if (session.getAttribute("loggedInAdmin") == null) {
//            return "redirect:/admin/login";
//        }
//
//        List<Book> books = bookService.findAllBooks();
//        model.addAttribute("books", books);
//        return "admin_books";
//    }
//
//    // === ADD BOOK ===
//    @PostMapping("/books/add")
//    public String addBook(@ModelAttribute Book book, HttpSession session) {
//        if (session.getAttribute("loggedInAdmin") == null) {
//            return "redirect:/admin/login";
//        }
//
//        bookService.saveBook(book);
//        return "redirect:/admin/books";
//    }
//
//    //    @PostMapping("/books/delete")
////    public String deleteBook(@RequestParam Long id, HttpSession session) throws BookNotFoundException {
////        if (session.getAttribute("loggedInAdmin") == null) {
////            return "redirect:/admin/login";
////        }
////
////        bookService.deleteBook(id);
////        return "redirect:/admin/books";
////    }
////    @DeleteMapping("/books/delete/{id}")
////    public String deleteBook(@PathVariable Long id, HttpSession session) throws BookNotFoundException {
////        if (session.getAttribute("loggedInAdmin") == null) {
////            return "redirect:/admin/login";
////        }
////
////        // The logic inside remains the same, but we now use @PathVariable
////        bookService.deleteBook(id);
////        return "redirect:/admin/books";
////    }
//// === DELETE BOOK ===
//    @DeleteMapping("/books/delete/{id}")
//    public String deleteBook(@PathVariable Long id, HttpSession session) throws BookNotFoundException {
//        if (session.getAttribute("loggedInAdmin") == null) {
//            return "redirect:/admin/login";
//        }
//
//        bookService.deleteBook(id);
//        return "redirect:/admin/books";
//    }
//
//    //    @PostMapping("/books/edit")
////    public String editBookRedirect(@RequestParam Long id, Model model, HttpSession session) throws BookNotFoundException {
////        if (session.getAttribute("loggedInAdmin") == null) {
////            return "redirect:/admin/login";
////        }
////
////        Book book = bookService.getBookById(id);
////        model.addAttribute("book", book);
////        return "admin_edit_book";
////    }
//    @GetMapping("/books/edit/{id}")
//    public String showEditBookForm(@PathVariable Long id, Model model, HttpSession session) throws BookNotFoundException {
//        if (session.getAttribute("loggedInAdmin") == null) {
//            return "redirect:/admin/login";
//        }
//
//        Book book = bookService.getBookById(id);
//        model.addAttribute("book", book);
//        return "admin_edit_book"; // The name of your Thymeleaf template for the edit form
//    }
//
//    // === UPDATE BOOK (Handles submission from the edit form) ===
//    @PutMapping("/books/update")
//    public String updateBook(@ModelAttribute Book book, HttpSession session) throws BookNotFoundException {
//        if (session.getAttribute("loggedInAdmin") == null) {
//            return "redirect:/admin/login";
//        }
//
//        // The 'book' object from the form should contain the ID.
//        // The service method will handle finding the book by its ID and updating its fields.
//        bookService.updateBook(book.getId(), book);
//        return "redirect:/admin/books";
//    }
//
//    // === VIEW ALL CUSTOMERS (READ-ONLY) ===
//    @GetMapping("/customers")
//    public String viewCustomers(Model model, HttpSession session) {
//        if (session.getAttribute("loggedInAdmin") == null) {
//            return "redirect:/admin/login";
//        }
//
//        List<Customer> customers = customerService.getAllCustomers();
//        model.addAttribute("customers", customers);
//        return "admin_customers";
//    }
//}