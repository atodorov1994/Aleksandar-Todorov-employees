# Employee Management System

This is a Spring Boot application for managing employee data. It demonstrates the use of Java, Spring Boot, and RESTful services to solve a real-world programming challenge.

## 🧰 Tech Stack

- Java 17+
- Spring Boot
- Maven
- REST APIs

## 🚀 Features

- Load employee data from CSV file
- Identify the pair of employees who have worked together the longest on common projects
- Efficient file parsing and processing
- Modular and testable codebase


## 📂 Input Format

The application expects a CSV file with the following columns:
```
EmpID, ProjectID, DateFrom, DateTo
```

Example:
```
143, 12, 2013-11-01, 2014-01-05
218, 10, 2012-05-16, NULL
```


## 🧪 Running the Application

### Prerequisites

- Java 17 or higher
- Maven installed

### Build and Run

```bash
# Clone the repository
git clone https://github.com/atodorov1994/Aleksandar-Todorov-employees.git
cd Aleksandar-Todorov-employees

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```
You can also run the main class directly from your IDE:
Main.java








