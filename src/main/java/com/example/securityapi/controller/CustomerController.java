package com.example.securityapi.controller;

import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.model.Book;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CustomerService;
import com.example.securityapi.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/")
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;
    private final BookService bookService;
    public CustomerController(CustomerService customerService, BookService bookService) {
        this.customerService = customerService;
        this.bookService = bookService;
    }
    // Make loggedInUser available to all views
    @ModelAttribute
    public void addLoggedInUserToModel(HttpSession session, Model model) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser", loggedInUser);
    }

    // Home page

//    @GetMapping
//    public String home() {
//        return "index";
//    }
    @GetMapping("")
    public String viewHomePage(@RequestParam(name = "keyword", required = false) String keyword,
                               Model model, HttpSession session) {
        List<Book> books;
        if (keyword != null && !keyword.isEmpty()) {
            // Fetch books matching title or author containing the keyword
            books = bookService.searchBooks(keyword);  // e.g., uses repository to search in title or author
        } else {
            // No keyword provided, fetch all books
            books = bookService.findAllBooks();
        }
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);  // preserve the search term in the view
        return "index";  // Render index.html Thymeleaf template
    }
    // Customer list page
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customers";
        // TODO: Restrict this page to ADMIN only in the future with Spring Security
    }

    // Show registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "register";
    }

    // Process registration
    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer) {
        customerService.saveCustomer(customer);
        return "redirect:/login";
    }

    // Show login form
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("customer", new Customer()); // Needed for th:object
        return "login";
    }

//    @PostMapping("/login")
//    public String loginCustomer(@ModelAttribute("customer") Customer customer,
//                                HttpServletRequest request,
//                                Model model) {
//
//        String username = customer.getUsername();
//        String password = customer.getPassword();
//
//        logger.info("Customer '{}' is attempting to log in", username);
//
//        boolean authenticated = customerService.authenticateCustomer(username, password);
//
//        if (authenticated) {
//            request.getSession().invalidate();
//            request.getSession(true).setAttribute("loggedInUser", username);
//            logger.info("Customer '{}' logged in successfully", username);
//            return "redirect:/";
//        } else {
//            model.addAttribute("customer", new Customer());
//            model.addAttribute("error", "Invalid username or password!");
//            return "login";
//        }
//    }
@PostMapping("/login")
public String loginCustomer(@ModelAttribute("customer") Customer customer,
                            HttpServletRequest request,
                            Model model) {
    String username = customer.getUsername();
    String password = customer.getPassword();
    logger.info("Customer '{}' is attempting to log in", username);
    boolean authenticated = customerService.authenticateCustomer(username, password);
    if (authenticated) {
        request.getSession().invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInUser", username);
        Customer loggedIn = customerService.findByUsername(username);
        session.setAttribute("isAdmin", loggedIn.isAdmin());
        logger.info("Customer '{}' logged in successfully", username);
        if (loggedIn.isAdmin()) {
            return "redirect:/admin/books";
        }
        return "redirect:/";
    } else {
        model.addAttribute("customer", new Customer());
        model.addAttribute("error", "Invalid username or password!");
        return "login";
    }
}


    @GetMapping("/customLogout")
    public String logout(HttpServletRequest request) {
        logger.info("Customer '{}' Logout");
        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Customer '{}' Logout", session.getAttribute("loggedInUser"));
            session.invalidate();
        }
        return "redirect:/login?logout";
    }






    // Admin only

    @GetMapping("/admin/books")
    public String bookList(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        List<Book> books = bookService.findAllBooks();
        model.addAttribute("books", books);
        return "admin_books";
    }

//    @PostMapping("/admin/books/add")
//    public String addBook(@ModelAttribute Book book, HttpSession session) {
//        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
//            return "redirect:/login";
//        }
//
//        bookService.saveBook(book);
//        return "redirect:/admin/books";
//    }
    @PostMapping("/admin/books/add")
        public String addBook(@ModelAttribute Book book, Model model) {
        // Check for existing book with same title, author, and year
        if (bookService.bookExists(book.getTitle(), book.getAuthor(), book.getYear())) {
            model.addAttribute("error", "A book with the same title, author, and year already exists.");
            model.addAttribute("book", book);
            return "admin_book_form"; // Ensure this matches your actual form template name
        }

        bookService.saveBook(book);
        return "redirect:/admin/books";
    }
    @GetMapping("/admin/books/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model, HttpSession session) throws BookNotFoundException {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "admin_edit_book";
    }

    @PutMapping("/admin/books/update")
    public String updateBook(@ModelAttribute Book book, HttpSession session) throws BookNotFoundException {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        bookService.updateBook(book.getId(), book);
        return "redirect:/admin/books";
    }
    @DeleteMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, HttpSession session) throws BookNotFoundException {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }
    @GetMapping("/admin/customers")
    public String viewCustomers(Model model, HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin_customers";
    }
}
