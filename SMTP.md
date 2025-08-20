# MFA and Email Setup Guide

### **Back to readme.md: [README.md](README.md)**

This document explains how the Multi-Factor Authentication (MFA) email setup works, how IntelliJ environment variables connect to `application.properties`, and how the Google 16-character app password is created. It also covers MySQL grants needed for persistence.

---

## 1. Environment Variables in IntelliJ

In IntelliJ IDEA, we configure environment variables in the **Run/Debug Configurations** dialog.  
For example, you defined:

- `MAIL_USERNAME=youraddress@gmail.com`
- `MAIL_APP_PASSWORD=xxxxxxxxxxxxxxxx` (the 16-char Google app password)
- `APP_MAIL_FROM=youraddress@gmail.com`

These variables are injected into the runtime process.  
Spring Boot reads them in `application.properties` using the syntax:

```properties
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_APP_PASSWORD:}
```

The `${VAR:}` means:
- Look for `VAR` in system environment variables.
- If not found, fall back to empty string.

So, when you run your app from IntelliJ, those IntelliJ environment variables are **passed into the JVM**, and Spring can resolve them.

---

## 2. Google App Password Creation (16 characters)

Because Google no longer allows "less secure apps", you must use an **App Password**.

Steps we followed:

1. Log in to your Gmail/Google account.
2. Go to **Google Account → Security**.
3. Enable **2-Step Verification** (if not already enabled).
4. Under *“Signing in to Google”* select **App passwords**.
5. Choose `Mail` as the app, and `Other` (or your device name) as the device.
6. Google generates a **16-character password** (spaces don’t matter).
7. Copy that password into your IntelliJ environment variable `MAIL_APP_PASSWORD`.

This is what Spring uses as the SMTP password.

---

## 3. Mail “From” Address

We also set:

```properties
app.mail.from=${APP_MAIL_FROM:}
```

This defines the email sender address.  
⚠️ Important: To avoid SPF/DMARC issues, the `APP_MAIL_FROM` should be the **same Gmail address** as `MAIL_USERNAME`.

---

## 4. MySQL Permissions

Regarding the MySQL permissions you asked about:  
You granted ALTER, CREATE, and INDEX rights. For development, you typically need a wider set of privileges (especially if Hibernate is creating or updating tables).

Recommended:

```sql
GRANT SELECT, INSERT, UPDATE, DELETE, ALTER, CREATE, INDEX
ON securityapi.* TO 'jimboy3100'@'%';
FLUSH PRIVILEGES;
```

- Replace `'%'` with `'localhost'` for better security if only running locally.

---

## 5. Optional Mail Properties

You may also add these to avoid timeouts:

```properties
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

And if port 587 is blocked, switch to SSL:

```properties
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
spring.mail.properties.mail.smtp.starttls.enable=false
```

---

## 6. Summary

- IntelliJ environment variables connect directly to `application.properties` using `${VAR:}` placeholders.
- Google App Password (16 characters) is required for Gmail SMTP.
- `MAIL_USERNAME` and `APP_MAIL_FROM` should be the same Gmail address.
- MySQL user needs sufficient privileges for schema updates and queries.
- With this setup, MFA codes are delivered by email via Gmail’s SMTP server securely.

## 7. Troubleshooting
Don't hesitate to contact me for any issue. I will be grad to help you. My contact information is stored at the bottom page of [README.md](README.md) 
