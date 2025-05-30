# 📄 Secure BookStore Project

A web-based book store implemented using **Spring Boot**, **Thymeleaf**, and **MySQL**, built as part of the module:

> **COMP47910 - Secure Software Engineering**
> MSc in Advanced Software Engineering, Summer 2024/25
> ![UCD Logo](https://www.ucd.ie/t4media/UCD_Logo_Colour.png)
> **University College Dublin (UCD)**

---

## ⚙️ Technologies & Libraries

| Tool / Library          | Purpose                        |
| ----------------------- | ------------------------------ |
| 🧰 Spring Boot 3.5.0    | Core backend framework         |
| 📝 Thymeleaf            | Server-side HTML templates     |
| 🛢 MySQL                | Relational database            |
| 🎨 Bootstrap 5.3.3      | UI styling                     |
| 🌟 Font Awesome 6.7.2   | Icons library                  |
| 📦 WebJars              | Frontend dependency management |
| 🧠 Java 21 (OpenJDK 21) | Programming language & runtime |

---

## 🛠️ Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/secure-bookstore.git
cd secure-bookstore
```

### 2. Configure the database

Create a **MySQL** database and update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build and run the project

```bash
./mvnw spring-boot:run
```

Visit [http://localhost:8080](http://localhost:8080) to access the application.

---

## 📅 Academic Context

This project is a course assignment developed for:

* **Module**: COMP47910 - Secure Software Engineering
* **Programme**: MSc in Advanced Software Engineering
* **Institution**: University College Dublin (UCD)
* **Semester**: Summer Trimester 2024/25

---

## 🚀 Features Implemented

* User login & session management
* Book browsing with search capability
* Add to cart with stock validation
* Quantity update with client-side subtotal and total calculation
* Mock checkout with card input validation

---

## ✉️ Contact

For questions related to this project, please contact:
**Kyriakidis Dimitrios (jimboy3100)**
📧 Email: [dimitrios.kyriakidis@ucdconnect.ie](mailto:dimitrios.kyriakidis@ucdconnect.ie)

---
