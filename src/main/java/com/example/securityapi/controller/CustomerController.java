package com.example.securityapi.controller;
import com.example.securityapi.exception.BookNotFoundException;
import com.example.securityapi.model.Author;
import com.example.securityapi.model.Book;
import com.example.securityapi.model.Customer;
import com.example.securityapi.service.AuthorService;
import com.example.securityapi.service.BookService;
import com.example.securityapi.service.CustomerService;
import com.example.securityapi.utilities.CaptchaService;
import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.access.prepost.PreAuthorize; // method-level security (new Aug 13)
import static com.example.securityapi.utilities.LogSanitizer.s;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
@Controller
@RequestMapping("/")
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;
    private final BookService bookService;
    private final AuthorService authorService;
    private final CaptchaService captchaService;
    // NOTE: Your constructor previously did not inject ChartHistoryService here, so we keep it as-is.
    public CustomerController(CustomerService customerService,
                              BookService bookService,
                              AuthorService authorService,
                              CaptchaService captchaService) {
        this.customerService = customerService;
        this.bookService = bookService;
        this.authorService = authorService;
        this.captchaService = captchaService;
    }
    // Make loggedInUser available to all views
    @ModelAttribute
    public void addLoggedInUserToModel(HttpSession session, Model model) {
        Object loggedInUserObj = session.getAttribute("loggedInUser");
        if (loggedInUserObj instanceof String loggedInUser) {
            model.addAttribute("loggedInUser", loggedInUser);
        }
    }
    // Home page
    @GetMapping("")
    public String viewHomePage(@RequestParam(name = "keyword", required = false) String keyword,
                               Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        List<Book> books = (keyword != null && !keyword.isEmpty())
                ? bookService.searchBooks(keyword)
                : bookService.findAllBooks();
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "customers";
        // TODO (legacy note): Now effectively protected by @PreAuthorize on admin endpoints.
    }
    // Show a registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session) {
        model.addAttribute("customer", new Customer());
        captchaService.generateCaptcha(session);   // generate CAPTCHA challenge
        return "register";
    }
    @PostMapping("/register")
    public String registerCustomer(@Valid @ModelAttribute("customer") Customer customer,
                                   BindingResult result,
                                   @RequestParam(name = "captcha", required = false) String captchaInput,
                                   HttpSession session,
                                   Model model) {
        // 1️⃣ CAPTCHA check first
        if (captchaService.validateCaptchaCustom(captchaInput, session)) {
            model.addAttribute("error", "Invalid CAPTCHA. Please try again.");
            captchaService.generateCaptcha(session); // new challenge
            return "register";
        }
        // 2️⃣ Bean validation
        if (result.hasErrors()) {
            return "register";
        }
        // 3️⃣ Domain validation
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
        // 4️⃣ Save customer (hashing handled in service)
        customerService.saveCustomer(customer);
        return "redirect:/login";
    }
    // Show a login form ✅ generate CAPTCHA
    @GetMapping("/login")
    public String showLoginForm(Model model, HttpSession session) {
        model.addAttribute("customer", new Customer());
        captchaService.generateCaptcha(session); // store code in session (used by CaptchaValidationFilter)
        return "login";
    }
    // 🔹 CAPTCHA image endpoint (so you can show an image in the form)
    @GetMapping("/captcha-image")
    public void captchaImage(HttpSession session, HttpServletResponse response) throws IOException {
        BufferedImage image = captchaService.generateCaptchaImage(session);
        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }
    // Keep your custom logout link/behavior
    @GetMapping("/customLogout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("loggedInUser");
            logger.info("Customer '{}' Logout", s(u)); // keep your log
            session.invalidate();
        }
        // 🔒 remove JSESSIONID, so no "invalid session" redirect happens
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0) // delete it immediately
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/login?logout";
    }
    // ===== Admin pages (protected by @PreAuthorize; legacy checks kept as comments for showcase) =====
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/books")
    public String bookList(Model model, HttpSession session) {
        model.addAttribute("books", bookService.findAllBooks());
        model.addAttribute("allAuthors", authorService.findAll());
        Book newBook = new Book();
        newBook.setYear(LocalDate.now().getYear());
        newBook.setPrice(20.00);
        newBook.setCopies(1);
        model.addAttribute("newBook", newBook);
        return "admin_books";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/books/add")
    public String addBook(@ModelAttribute Book book, Model model) {
        if (bookService.bookExists(book.getTitle(), book.getAuthors(), book.getYear())) {
            model.addAttribute("error", "A book with the same title, authors, and year already exists.");
            model.addAttribute("book", book);
            return "admin_book_form";
        }
        bookService.saveBook(book);
        return "redirect:/admin/books";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/books/edit/{id}")
    public String showEditBookForm(@PathVariable("id") Long id, Model model, HttpSession session) throws BookNotFoundException {
        Book book = bookService.getBookById(id);
        List<Author> allAuthors = authorService.findAll();
        model.addAttribute("book", book);
        model.addAttribute("allAuthors", allAuthors);

        return "admin_edit_book";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/books/update")
    public String updateBook(@Valid @ModelAttribute("book") Book book,
                             BindingResult bindingResult,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) throws BookNotFoundException {
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, HttpSession session) throws BookNotFoundException {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/customers")
    public String viewCustomers(Model model, HttpSession session) {
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "admin_customers";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/authors")
    public String manageAuthors(Model model, HttpSession session,
                                @ModelAttribute("error") String errorMessage) {
        List<Author> authors = authorService.findAll();
        model.addAttribute("authors", authors);
        model.addAttribute("newAuthor", new Author());
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "admin_manage_authors";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/authors/add")
    public String addAuthor(@ModelAttribute Author author,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        if (authorService.exists(author.getFirstName(), author.getLastName())) {
            redirectAttributes.addFlashAttribute("error", "Author already exists.");
            return "redirect:/admin/authors";
        }
        authorService.add(author);
        return "redirect:/admin/authors";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/authors/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
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
}
