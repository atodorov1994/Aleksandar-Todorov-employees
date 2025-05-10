package com.employees.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeesResponse {

    private Employee employeeIdFirst;
    private Employee employeeIdSecond;
    private Long projectId;
    private Long daysWorkedTogether;
}
