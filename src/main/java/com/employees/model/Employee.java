package com.employees.model;

import com.employees.services.CSVService;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

@Component
@Data
public class Employee implements Comparable<Employee>{
    @CsvBindByPosition(position = 0)
    private String id;
    @CsvBindByPosition(position = 1)
    private String projectId;
    @CsvBindByPosition(position = 2)
    private String dateFrom;
    @CsvBindByPosition(position = 3)
    private String dateTo;

    @Override
    public int compareTo(Employee o) {
        int compareFromDate = CSVService.parseDate(this.getDateFrom()).compareTo(CSVService.parseDate(o.getDateFrom()));
        int compareEmplId = this.getId().compareTo(o.getId());
        int compareProjectId = this.getProjectId().compareTo(o.getProjectId());
        if(compareEmplId == 0){
            if (compareProjectId == 0){
                return 0;
            }
        }
        else if (compareFromDate == 0) {
            return 1;
        }
        return compareFromDate;
    }
}
