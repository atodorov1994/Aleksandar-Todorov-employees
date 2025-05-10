package com.employees.controller;

import com.employees.model.EmployeesResponse;
import com.employees.model.Response;
import com.employees.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<Response<EmployeesResponse>> findLongestWorkingEmployees(@RequestPart MultipartFile file) throws IOException {

        return ResponseEntity.ok(new Response<>(0, employeeService.showEmployeesWorkingTogether(file)));
    }
}
