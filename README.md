# CareerConnect - Backend API Architecture

CareerConnect is a modern, high-performance job portal designed to connect premium tech talent with innovative companies. The backend is built using a robust, scalable N-Tier architecture driven by **Spring Boot 3** and **MongoDB**, acting as the central nervous system for data persistence, security, AI processing, and real-time messaging.

## 🌟 Comprehensive Feature Set

### 1. Robust Authentication & Security
- **JWT (JSON Web Token):** Stateless authentication. Users are verified via tokens on every secure request, minimizing database load.
- **OTP Verification Engine:** Secure email-based OTP (One Time Password) login and signup flow via `JavaMailSender`. Password resets also require OTP validation to generate secure reset tokens.
- **Spring Security:** Strict method-level and route-level security, ensuring applicants cannot modify company job postings and unauthorized users cannot access private messages.
- **Rate Limiting:** Protects against Brute Force and Spam attacks using `Bucket4j`. Login attempts are limited to 10 per hour, and OTP requests are limited to 3 per hour per email address.

### 2. Intelligent AI Integrations (Powered by Groq API)
- **AI Match Score Generator:** Takes an applicant's entire profile summary and a job description, sending them to the Groq LLM API to return a precise match score, specific strengths, and areas for improvement.
- **Resume Parsing Engine:** Extracts plain text from uploaded PDFs using `Apache PDFBox` and leverages the LLM to parse unstructured text into highly structured JSON (`skills`, `experiences`, `jobTitle`).
- **AI Job Summarizer:** Automatically condenses lengthy, complex job descriptions into bite-sized, readable summaries.

### 3. Core Domain Capabilities
- **Job Management:** CRUD operations for Job Postings. Employers can publish, edit, and close jobs. Applicants can search for jobs, filter by skills, and apply directly.
- **Profile Management:** Comprehensive tracking of a user's `skills`, `certifications`, `experiences`, and saved/applied jobs.
- **Messaging Engine:** Powers the real-time chat feature. Creates secure chat rooms between Employers and Applicants, persists message histories, and tracks the last active timestamps.
- **Search & Filtering:** Complex MongoDB aggregation pipelines support natural language filtering or strict parameter matching for job and talent searches.

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.3.2
- **Language:** Java 17
- **Database:** MongoDB (Spring Data MongoDB)
- **Security:** Spring Security, JJWT (io.jsonwebtoken)
- **Rate Limiting:** Bucket4j (`com.bucket4j`)
- **Email Service:** Spring Boot Starter Mail (`JavaMailSender`)
- **PDF Extraction:** Apache PDFBox 3.0.2
- **Lombok:** Reduces boilerplate code (`@Data`, `@NoArgsConstructor`).

---

## 🏗️ Architecture & Design Patterns

The backend follows a strict **N-Tier (Layered) Architecture**:

1. **Controllers (`src/main/java/com/jobportal/api/`):** 
   - Defines REST endpoints.
   - Handles incoming HTTP requests and standardizes responses (`ResponseEntity`).
2. **Services (`src/main/java/com/jobportal/service/`):** 
   - Contains all business logic (e.g., AI score calculation, rate limit enforcement).
   - Abstractions ensure controllers remain lightweight.
3. **Repositories (`src/main/java/com/jobportal/repository/`):** 
   - Data Access Layer using `MongoRepository`.
   - Handles custom `@Query` definitions and database interactions.
4. **Entities / DTOs (`src/main/java/com/jobportal/entity/` & `dto/`):** 
   - Represents the MongoDB document schemas (`User`, `JobPost`, `Profile`).
   - DTOs (Data Transfer Objects) are used to transfer data between the client and server without exposing internal database structures.
5. **Security & JWT Configurations (`src/main/java/com/jobportal/jwt/`):** 
   - Contains the `JwtAuthenticationFilter` that intercepts requests, validates the Bearer token, and populates the `SecurityContext`.

---

## 🚀 Getting Started

### Prerequisites
- Java 17 (JDK 17)
- Maven 3.8+
- A MongoDB Cluster (Local or MongoDB Atlas)
- Groq API Key (for AI features)
- Gmail App Password (for OTP emails)

### Installation & Configuration

1. **Clone & Navigate:**
   ```bash
   git clone <repository-url>
   cd backend
   ```
2. **Configure Environment Variables:**
   You must provide the following environment variables or configure them directly in `src/main/resources/application.properties`:
   - `MONGO_PASSWORD`: Your MongoDB connection password.
   - `EMAIL_USERNAME`: The Gmail address used to send OTPs.
   - `EMAIL_PASSWORD`: The 16-character Google App Password.
   - `GROQ_API_KEY`: Your free Groq LLM API Key.
   - *Optional:* `PORT` (defaults to 8080).

   > **Note on Windows IPv6:** 
   > The application uses `System.setProperty("java.net.preferIPv4Stack", "true");` in the main class to prevent SMTP connection timeouts common on Windows IPv6 configurations.

3. **Install Dependencies & Build:**
   ```bash
   mvn clean install -DskipTests
   ```
4. **Run the Server:**
   ```bash
   mvn spring-boot:run
   ```
5. The backend will start on `http://localhost:8080`.

---

## 🔒 API Security & Access

All endpoints outside of `/users/login`, `/users/sendOtp`, and `/users/register` are secured. 
Clients must include the Authorization header to interact with protected resources:
```http
Authorization: Bearer <your_jwt_token_here>
```

## 📦 Build for Production (Deployment)

To package the Spring Boot application into a standalone, executable JAR:
```bash
mvn clean package -DskipTests
```
The output JAR will be placed in the `target/` directory. You can deploy this JAR to Render, AWS EC2, or any Dockerized container service:
```bash
java -jar target/JobPortal-0.0.1-SNAPSHOT.jar
```
