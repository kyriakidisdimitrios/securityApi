
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

> *(Insert screenshots or link to hosted demo if available)*

---

✅ Now you're all set. If anything breaks, it's probably the database or Java setup—double check `JAVA_HOME`, `Path`, and your database URL/credentials!
