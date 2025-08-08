package com.example.securityapi.controller;

import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.model.Author;
import com.example.securityapi.model.Book;
import com.example.securityapi.model.ChartHistory;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.AuthorService;
import com.example.securityapi.service.ChartHistoryService;
import com.example.securityapi.service.CustomerService;
import com.example.securityapi.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.commons.text.StringEscapeUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;
    private final BookService bookService;
    private final AuthorService authorService;
    public CustomerController(CustomerService customerService, BookService bookService, AuthorService authorService) {
        this.customerService = customerService;
        this.bookService = bookService;
        this.authorService = authorService;
    }
    // Make loggedInUser available to all views
    @ModelAttribute
    public void addLoggedInUserToModel(HttpSession session, Model model) {
        // BEFORE: No null check or type check
        // String loggedInUser = (String) session.getAttribute("loggedInUser");
        // model.addAttribute("loggedInUser", loggedInUser);
        // AFTER: Added null and type safety
        Object loggedInUserObj = session.getAttribute("loggedInUser");  // ADDED
        if (loggedInUserObj instanceof String loggedInUser) {           // ADDED
            model.addAttribute("loggedInUser", loggedInUser);           // ADDED
        }                                                               // ADDED
    }
    // Home page

//    @GetMapping
//    public String home() {
//        return "index";
//    }
@GetMapping("")
public String viewHomePage(@RequestParam(name = "keyword", required = false) String keyword,
                           Model model, HttpSession session) {
    // 🔐 redirect anonymous users to login
    if (session.getAttribute("loggedInUser") == null) {
        return "redirect:/login";
    }

    List<Book> books;
    if (keyword != null && !keyword.isEmpty()) {
        books = bookService.searchBooks(keyword);
    } else {
        books = bookService.findAllBooks();
    }

    List<Book> filteredBooks = books.stream()
            .filter(book ->
                    book != null &&
                            book.getTitle() != null &&
                            book.getAuthors() != null &&
                            !book.getAuthors().isEmpty() &&
                            book.getPrice() != null
            )
            .toList();

    model.addAttribute("books", filteredBooks);
    model.addAttribute("keyword", keyword);
    return "index";
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
    public String showRegisterForm(Model model) { //Manually add empty object to the view. 	Server → View
        model.addAttribute("customer", new Customer());
        return "register";
    }

    // Process registration
//    @PostMapping("/register")
//    public String registerCustomer(@ModelAttribute("customer") Customer customer) { //binds data from form inputs. View → Server
//        customerService.saveCustomer(customer);
//        return "redirect:/login";
//    }
    @PostMapping("/register")
    public String registerCustomer(@Valid @ModelAttribute("customer") Customer customer,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (customer.getDateOfBirth().isBefore(LocalDate.of(1900, 1, 1)) ||
                customer.getDateOfBirth().isAfter(LocalDate.of(2010, 12, 31))) {
            result.rejectValue("dateOfBirth", "error.customer", "Date of birth must be between 1900 and 2010");
            return "register";
        }

        if (customerService.findByUsername(customer.getUsername()) != null) {
            result.rejectValue("username", "error.customer", "Username already exists");
            return "register";
        }

        if (customerService.findByPhoneNumber(customer.getPhoneNumber()) != null) {
            result.rejectValue("phoneNumber", "error.customer", "Phone number already exists");
            return "register";
        }
        if (customerService.findByEmail(customer.getEmail()) != null) {
            result.rejectValue("email", "error.customer", "Email already exists");
            return "register";
        }

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

    final String rawUsername = customer.getUsername();
    final String rawPassword = customer.getPassword();

    if (rawUsername == null || rawPassword == null) {
        model.addAttribute("error", "Username and password must not be null.");
        return "login";
    }

    final String sanitizedUsername = StringEscapeUtils.escapeHtml4(rawUsername.trim());
    final String sanitizedPassword = rawPassword.trim();

    logger.info("Customer '{}' is attempting to log in", sanitizedUsername);

    boolean authenticated = customerService.authenticateCustomer(sanitizedUsername, sanitizedPassword);

    if (!authenticated) {
        model.addAttribute("error", "Invalid username or password!");
        return "login";
    }

    request.getSession().invalidate();
    HttpSession session = request.getSession(true);

    Customer loggedIn = customerService.findByUsername(sanitizedUsername);
    if (loggedIn == null) {
        model.addAttribute("error", "Unexpected error. Try again.");
        return "login";
    }

    session.setAttribute("loggedInUser", sanitizedUsername);  // already escaped
    session.setAttribute("isAdmin", loggedIn.isAdmin());

    logger.info("Customer '{}' logged in successfully", sanitizedUsername);

    return loggedIn.isAdmin() ? "redirect:/admin/books" : "redirect:/";
}



    @GetMapping("/customLogout")
    public String logout(HttpServletRequest request) {
        // BEFORE: logger printed null
        // logger.info("Customer '{}' Logout");

        HttpSession session = request.getSession(false);
        if (session != null) {
            logger.info("Customer '{}' Logout", session.getAttribute("loggedInUser"));  // FIXED
            session.invalidate();
        }
        return "redirect:/login?logout";
    }

    // Admin only

    @GetMapping("/admin/books")
    public String bookList(Model model, HttpSession session) {
        // Admin check
        // BEFORE: Weak admin check or none
        // if (!session.getAttribute("isAdmin").equals(true)) { ... }

        // AFTER: Robust admin check using Boolean.TRUE.equals()
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {  // FIXED
            return "redirect:/login";
        }

        // This part for the table remains the same
        model.addAttribute("books", bookService.findAllBooks());
        model.addAttribute("allAuthors", authorService.findAll());

        // --- START: The Fix for Default Values ---

        // 1. Create a new Book instance
        Book newBook = new Book();

        // 2. Set the desired default values on the object
        newBook.setYear(LocalDate.now().getYear()); // Sets the current year (e.g., 2024)
        newBook.setPrice(20.00);                    // Sets the default price to 20.00
        newBook.setCopies(1);                       // Sets the default quantity to 1

        // 3. Add the pre-populated book object to the model
        model.addAttribute("newBook", newBook);

        // --- END: The Fix ---

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
    // Check for existing book with same title, authors, and year
    if (bookService.bookExists(book.getTitle(), book.getAuthors(), book.getYear())) {
        model.addAttribute("error", "A book with the same title, authors, and year already exists.");
        model.addAttribute("book", book);
        return "admin_book_form";
    }

    bookService.saveBook(book);
    return "redirect:/admin/books";
}
    @GetMapping("/admin/books/edit/{id}")
    public String showEditBookForm(@PathVariable("id")  Long id, Model model, HttpSession session) throws BookNotFoundException {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        // 1. Get the book to be edited
        Book book = bookService.getBookById(id);

        // --- FINAL DEBUGGING STEP ---
        // Print the ID of the book THE MOMENT it comes back from the service.
        //System.out.println("FETCHED BOOK FOR EDIT PAGE. ID IS: " + book.getId());
        // --- END DEBUGGING STEP ---

        // 2. Get all authors for the dropdown
        List<Author> allAuthors = authorService.findAll();

        // 3. Add the book and authors to the model
        model.addAttribute("book", book);
        model.addAttribute("allAuthors", allAuthors);

        return "admin_edit_book";
    }
    @PutMapping("/admin/books/update")
    public String updateBook(@Valid @ModelAttribute("book") Book book,
                             BindingResult bindingResult,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) throws BookNotFoundException {

        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Copies must be a positive whole number (e.g. 0, 1, 2...).");
            return "redirect:/admin/books/edit/" + book.getId();
        }

        Book existingBook = bookService.getBookById(book.getId());
        if (existingBook == null) {
            return "redirect:/admin/books?error=notfound";
        }

        existingBook.setTitle(book.getTitle());
        existingBook.setPrice(book.getPrice());
        existingBook.setAuthors(book.getAuthors());
        existingBook.setYear(book.getYear());
        existingBook.setCopies(book.getCopies());

        bookService.saveBook(existingBook);
        return "redirect:/admin/books";
    }
    @DeleteMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, HttpSession session) throws BookNotFoundException {
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
    @GetMapping("/admin/authors")
    public String manageAuthors(Model model, HttpSession session,
                                @ModelAttribute("error") String errorMessage) {
        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        List<Author> authors = authorService.findAll();
        model.addAttribute("authors", authors);
        model.addAttribute("newAuthor", new Author()); // for add form

        // ✅ Pass flash error to template as "errorMessage"
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }

        return "admin_manage_authors";
    }
    @PostMapping("/admin/authors/add")
    public String addAuthor(@ModelAttribute Author author,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {

        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        if (authorService.exists(author.getFirstName(), author.getLastName())) {
            redirectAttributes.addFlashAttribute("error", "Author already exists.");
            return "redirect:/admin/authors";
        }

        authorService.add(author);
        return "redirect:/admin/authors";
    }
    @DeleteMapping("/admin/authors/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {

        if (!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        Author author = authorService.findById(id);
        if (author == null) {
            redirectAttributes.addFlashAttribute("error", "Author not found.");
        } else if (!author.getBooks().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete author assigned to books.");
        } else {
            authorService.deleteById(id);
        }

        return "redirect:/admin/authors";
    }
    private String applySalt(String password) {
        final String fixedSalt = "S3cUr3S@lt!";  // Ideally from a config or env var
        return fixedSalt + password;
    }
}
