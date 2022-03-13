package com.employees.model;

import com.employees.services.CSVService;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

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
        LocalDateTime otherFrom = CSVService.parseDate(o.getDateFrom());
        LocalDateTime otherTo = CSVService.parseDate(o.getDateTo());
        LocalDateTime from = CSVService.parseDate(this.getDateFrom());
        LocalDateTime to = CSVService.parseDate(this.getDateTo());
//        Check for duplicated objects and remove them - employee is considered duplicated when:
//        -this.emplId = other.emplId
//        -this.projectId = other.projectId
//        -this and other (which are the same person) have overlapping intervals over the same project
//        We assume that one person could have been working on the same project in independent time intervals
        if(compareEmplId == 0){
            if (compareProjectId == 0){
                if (compareFromDate < 0){
                    if (to.isAfter(otherFrom)){
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }else if (compareFromDate > 0){
                    if (otherTo.isAfter(from)){
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }
                else {
                    return 0;
                }
            }
        }
//        Sort by formDate ASC - equal formDates still enter
        else if (compareFromDate == 0) {
            return 1;
        }
        return compareFromDate;
    }
}
