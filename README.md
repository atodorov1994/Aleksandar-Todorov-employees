# Employees working together

This is simple Vaadin application with Spring Boot.
The purpose is to find which of all employees have worked together for the longest period of days.
The input data is in .csv format.


## Project structure

- `MainLayout.java` in `src/main/java` contains the navigation setup (i.e., the
  side/top bar and the main menu).
- `CSVService.java` in `src/main/java` contains back-end logic of the application.
- `views` package in `src/main/java` contains the server-side Java views of the application.
- `model` package in `src/main/java` contains the Employee entity.
- `views` folder in `frontend/` contains the client-side JavaScript views of the application.
- `themes` folder in `frontend/` contains the custom CSS styles.
