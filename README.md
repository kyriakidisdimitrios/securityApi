
# 📄 Secure BookStore Project

A full-stack web application for managing and purchasing books online, implemented with:

- ✅ Spring Boot (Backend)
- ✅ Thymeleaf (Frontend)
- ✅ MySQL (Database)
- ✅ Bootstrap (UI styling)

> **Course**: COMP47910 - Secure Software Engineering  
> **Programme**: MSc in Advanced Software Engineering  
> **Semester**: Summer 2024/25  
> **University College Dublin (UCD)**  
> <img src="/src/main/resources/static/images/crest-ucd.svg" alt="UCD Logo" width="36"/>

---

## ⚙️ Technologies Used

| Tool / Library          | Purpose                        |
| ----------------------- | ------------------------------ |
| ☕ Java 21 (OpenJDK)     | Language and runtime platform  |
| 🧰 Spring Boot 3.5.0    | Backend development framework  |
| 📝 Thymeleaf            | Server-side template engine    |
| 🛢 MySQL                | Relational database            |
| 🎨 Bootstrap 5.3.3      | CSS styling                    |
| 🌟 Font Awesome 6.7.2   | Icons                          |
| 📦 WebJars              | Frontend dependency delivery   |
| 🔐 Spring Security      | Basic access/session protection |

---

## 🛠️ Installation & Setup (Beginner Friendly)

### ✅ Step 1: Install Java 21 (OpenJDK)

1. Visit the official OpenJDK builds:  
   👉 [https://jdk.java.net/21/](https://jdk.java.net/21/)

2. Download the correct installer for your OS (Windows/Linux/macOS).

3. **Extract or Install** the JDK, then set environment variables:

#### ✅ On Windows:

- Open *System Properties → Environment Variables*
- Under **System Variables**, click **New**:
    - **Name**: `JAVA_HOME`
    - **Value**: `C:\Program Files\Java\jdk-21` (adjust based on your path)
- Edit `Path` → Add:  
  `%JAVA_HOME%\bin`

Then confirm via terminal:

```bash
java -version
```

Should return something like:  
`java 21 2023-09-19` ✔️

---

### ✅ Step 2: Clone the Repository

```bash
git clone https://github.com/kyriakidisdimitrios/securityApi.git
cd securityApi
```

---

### ✅ Step 3: Install & Configure MySQL

1. Install MySQL from:  
   👉 [https://dev.mysql.com/downloads/installer/](https://dev.mysql.com/downloads/installer/)

2. Create a new database:

```sql
CREATE DATABASE bookstore;
```

Typical MySQL commands:
2a. 
Delete entires:
```sql
SET SQL_SAFE_UPDATES = 0;
DELETE FROM cart_items;
DELETE FROM book_authors;
DELETE FROM books;
DELETE FROM authors;
DELETE FROM customers;
SET SQL_SAFE_UPDATES = 1;

-- Reset auto-increment counters
ALTER TABLE cart_items AUTO_INCREMENT = 1;
ALTER TABLE book_authors AUTO_INCREMENT = 1;
ALTER TABLE books AUTO_INCREMENT = 1;
ALTER TABLE authors AUTO_INCREMENT = 1;
ALTER TABLE customers AUTO_INCREMENT = 1;
```

2b. Add a customer with privilede admin having username `admin` and password `admin`, and populate tables books, authors and book_authors.
```sql
-- 🧑‍💼CREATE ADMIN. USERNAME: admin PASSWORD: admin

-- 🧑‍💼 AUTHORS
INSERT INTO authors (first_name, last_name) VALUES ('J.R.R.', 'Tolkien');
INSERT INTO authors (first_name, last_name) VALUES ('George R.R.', 'Martin');
INSERT INTO authors (first_name, last_name) VALUES ('J.K.', 'Rowling');
INSERT INTO authors (first_name, last_name) VALUES ('Stephen', 'King');
INSERT INTO authors (first_name, last_name) VALUES ('Agatha', 'Christie');
INSERT INTO authors (first_name, last_name) VALUES ('Isaac', 'Asimov');
INSERT INTO authors (first_name, last_name) VALUES ('Arthur C.', 'Clarke');
INSERT INTO authors (first_name, last_name) VALUES ('Dan', 'Brown');
INSERT INTO authors (first_name, last_name) VALUES ('Jane', 'Austen');
INSERT INTO authors (first_name, last_name) VALUES ('Mark', 'Twain');

-- 📚 BOOKS
INSERT INTO books (title, year, price, copies) VALUES ('The Hobbit', 1937, 18.99, 10);
INSERT INTO books (title, year, price, copies) VALUES ('A Game of Thrones', 1996, 22.5, 8);
INSERT INTO books (title, year, price, copies) VALUES ('Harry Potter and the Philosopher Stone', 1997, 20.0, 12);
INSERT INTO books (title, year, price, copies) VALUES ('It', 1986, 19.95, 6);
INSERT INTO books (title, year, price, copies) VALUES ('Murder on the Orient Express', 1934, 14.5, 9);
INSERT INTO books (title, year, price, copies) VALUES ('Foundation', 1951, 17.75, 11);
INSERT INTO books (title, year, price, copies) VALUES ('2001: A Space Odyssey', 1968, 16.8, 7);
INSERT INTO books (title, year, price, copies) VALUES ('The Da Vinci Code', 2003, 21.6, 10);
INSERT INTO books (title, year, price, copies) VALUES ('Pride and Prejudice', 1813, 13.4, 8);
INSERT INTO books (title, year, price, copies) VALUES ('Adventures of Huckleberry Finn', 1884, 15.0, 6);
INSERT INTO books (title, year, price, copies) VALUES ('Collaborative Tales', 2024, 25.0, 5);

-- 🔗 BOOK-AUTHOR RELATIONSHIPS
INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'The Hobbit' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'J.R.R.' AND last_name = 'Tolkien' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'A Game of Thrones' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'George R.R.' AND last_name = 'Martin' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Harry Potter and the Philosopher Stone' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'J.K.' AND last_name = 'Rowling' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'It' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Stephen' AND last_name = 'King' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Murder on the Orient Express' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Agatha' AND last_name = 'Christie' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Foundation' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Isaac' AND last_name = 'Asimov' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = '2001: A Space Odyssey' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Arthur C.' AND last_name = 'Clarke' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'The Da Vinci Code' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Dan' AND last_name = 'Brown' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Pride and Prejudice' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Jane' AND last_name = 'Austen' LIMIT 1));

INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Adventures of Huckleberry Finn' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'Mark' AND last_name = 'Twain' LIMIT 1));

-- 📘 Collaborative book with two authors
INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Collaborative Tales' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'J.R.R.' AND last_name = 'Tolkien' LIMIT 1));
INSERT INTO book_authors (book_id, author_id)
VALUES ((SELECT id FROM books WHERE title = 'Collaborative Tales' LIMIT 1),
        (SELECT id FROM authors WHERE first_name = 'George R.R.' AND last_name = 'Martin' LIMIT 1));
```

3. Update your Spring config file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

### ✅ Step 4: Build and Run the Project

If you have Maven installed:

```bash
mvn spring-boot:run
```

If you're using the wrapper (preferred):

```bash
./mvnw spring-boot:run
```

Visit the app at:  
🔗 [http://localhost:8080](http://localhost:8080)

---

### ✅ Note: Maven uses your Java environment

> Make sure your project **and Maven** use the same JDK version (Java 21).
You can verify Maven uses the correct Java by running:

```bash
mvn -v
```

Ensure it outputs:
```
Java version: 21, vendor: Oracle Corporation (or OpenJDK)
```

---

## 📅 Academic Context

| Detail             | Info                                   |
|-------------------|----------------------------------------|
| 🎓 Module          | COMP47910 – Secure Software Engineering |
| 🏫 Institution     | University College Dublin (UCD)         |
| 📆 Semester        | Summer Trimester 2024/25                |
| 👨‍💻 Developer      | Kyriakidis Dimitrios (jimboy3100)        |

---

## 🚀 Features Overview

- 👤 User login/logout, admin/customer support
- 🔍 Book browsing with search
- 🛒 Cart system with quantity updates
- 💳 Mock checkout with card validation
- 🧾 Purchase history and chart data
- 🧪 Basic security via session control

---

## 📬 Contact

**Kyriakidis Dimitrios**  
📧 [dimitrios.kyriakidis@ucdconnect.ie](mailto:dimitrios.kyriakidis@ucdconnect.ie)

---

## 📷 Preview (Optional)

<img src="/src/main/resources/static/images/preview.png" alt="Store preview" width="900"/>

---

✅ Now you're all set. If anything breaks, it's probably the database or Java setup—double check `JAVA_HOME`, `Path`, and your database URL/credentials!
