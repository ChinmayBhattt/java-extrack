# 💸 SmartSpend 2.0 — Spring Boot Edition

> A professional-grade personal expense tracker built with Spring Boot, JWT authentication, JPA/Hibernate, and a modern dark-mode Thymeleaf UI.

---

## 🚀 Quick Start (Development)

### Prerequisites
| Tool        | Version      |
|-------------|--------------|
| Java        | 17 or higher |
| Maven       | 3.8+         |
| Any browser | Latest       |

> **No database setup required!** Uses H2 in-memory DB automatically.

### Run the App

```bash
# Navigate to project root
cd "e:/Projects/College Project/Smart Spend 2.0"

# Option A: Maven wrapper (recommended)
./mvnw spring-boot:run

# Option B: Standard Maven
mvn spring-boot:run
```

**App will start at:** `http://localhost:8080`

---

## 🔗 Key URLs

| URL                              | Description                     |
|----------------------------------|---------------------------------|
| `http://localhost:8080`          | Root — redirects to login       |
| `http://localhost:8080/login`    | Sign-in page                    |
| `http://localhost:8080/register` | Create account page             |
| `http://localhost:8080/dashboard`| Analytics dashboard (auth req.) |
| `http://localhost:8080/expenses` | Expense management              |
| `http://localhost:8080/budgets`  | Budget limit manager            |
| `http://localhost:8080/profile`  | User profile settings           |
| `http://localhost:8080/h2-console` | H2 dev DB browser             |

### H2 Console Login
- **JDBC URL:** `jdbc:h2:mem:smartspend`
- **Username:** `sa`
- **Password:** *(leave blank)*

---

## 📡 REST API Reference

### Authentication
```
POST /api/auth/register    Body: { name, email, password, salary? }
POST /api/auth/login       Body: { email, password }
POST /api/auth/logout      (clears cookie)
```

### Expenses (requires JWT)
```
GET    /api/expenses               All expenses (current user)
GET    /api/expenses?year=&month=  Filter by month
GET    /api/expenses?category=     Filter by category
GET    /api/expenses/{id}          Single expense
POST   /api/expenses               Create  { title, amount, category, expenseDate, notes? }
PUT    /api/expenses/{id}          Update
DELETE /api/expenses/{id}          Delete
```

### Dashboard Analytics (requires JWT)
```
GET    /api/dashboard    Returns: totals, savings, category breakdown, 6-month trend, advice
```

### Budgets (requires JWT)
```
GET    /api/budgets             List budgets with progress
POST   /api/budgets             Set budget  { category, limitAmount }
DELETE /api/budgets/{id}        Remove budget
```

### Profile (requires JWT)
```
GET    /api/users/me            Get profile
PUT    /api/users/me            Update { name?, salary?, newPassword? }
```

---

## 🏗️ Project Architecture

```
com.smartspend/
├── config/         SecurityConfig, WebConfig
├── controller/     AuthController, ExpenseController, DashboardController,
│                   BudgetController, UserController, PageController
├── dto/            auth/, expense/, budget/, dashboard/
├── exception/      GlobalExceptionHandler, custom exceptions
├── model/
│   ├── entity/     User, Expense, Budget
│   └── enums/      Category
├── repository/     UserRepository, ExpenseRepository, BudgetRepository
├── security/       JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
└── service/        AuthService, ExpenseService, DashboardService,
                    BudgetService, UserService
```

---

## 🗃️ Database Schema

```sql
-- Users
CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  salary        DECIMAL(12,2) DEFAULT 0.00,
  created_at    TIMESTAMP
);

-- Expenses
CREATE TABLE expenses (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  title        VARCHAR(200) NOT NULL,
  amount       DECIMAL(12,2) NOT NULL,
  category     VARCHAR(50)  NOT NULL,
  expense_date DATE         NOT NULL,
  notes        TEXT,
  user_id      BIGINT NOT NULL REFERENCES users(id),
  created_at   TIMESTAMP
);

-- Budgets (one per user per category)
CREATE TABLE budgets (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  category     VARCHAR(50) NOT NULL,
  limit_amount DECIMAL(12,2) NOT NULL,
  user_id      BIGINT NOT NULL REFERENCES users(id),
  UNIQUE (user_id, category)
);
```

---

## 🔒 Security

- **JWT** (HS256) stored in `HttpOnly` cookies — XSS-safe
- **BCrypt** password hashing (strength 10)
- All `/api/**` routes require a valid JWT except `/api/auth/**`
- CSRF disabled — JWT approach is CSRF-safe for REST

---

## 🐬 Switch to MySQL (Production)

1. Create a MySQL database named `smartspend`
2. Edit `src/main/resources/application-prod.properties`:
   ```properties
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```
3. Run with the prod profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 📦 Technology Stack

| Layer       | Technology                     |
|-------------|--------------------------------|
| Backend     | Spring Boot 3.2, Spring MVC    |
| Security    | Spring Security + JWT (jjwt)   |
| Database    | H2 (dev) / MySQL (prod)        |
| ORM         | JPA / Hibernate                |
| Frontend    | Thymeleaf + Chart.js           |
| CSS         | Vanilla CSS (dark mode)        |
| Build Tool  | Maven                          |
| Java        | 17 (LTS)                       |
# java-extrack
