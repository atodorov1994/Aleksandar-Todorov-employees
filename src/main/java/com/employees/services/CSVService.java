package com.employees.services;

import com.employees.exception.BadRequestException;
import com.employees.model.Employee;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.server.VaadinRequest;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CSVService {
    private static final Map<String , Set<String[]>> DB = new ConcurrentHashMap<>();

    @SneakyThrows
    public void showEmployeesWorkingTogether(InputStream inputStream , Grid<String[]> grid , String fileName , VaadinRequest request) {
        var parser = new CSVParserBuilder().withSeparator(',').build();
        var reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(parser).build();
        List<Employee> employees = new CsvToBeanBuilder(reader).withType(Employee.class).build().parse();
        Map<String , Set<Employee>> employeesByProject = new HashMap<>();
        for (int i = 1; i < employees.size(); i++) {
            Employee e = employees.get(i);
            String key = e.getProjectId();
            if (!employeesByProject.containsKey(key)){
                employeesByProject.put(key , new TreeSet<>());
                employeesByProject.get(key).add(e);
            }
            else {
                employeesByProject.get(key).add(e);
            }
        }
        long maxDuration = 0;
        Employee e1 = null;
        Employee e2 = null;
        for (Set<Employee> set : employeesByProject.values()) {
            if (set.size() > 1) {
                List<Employee> employeesSortedByFromDateAsc = new ArrayList<>(set);
                for (int i = 0; i < employeesSortedByFromDateAsc.size(); i++) {
                    LocalDateTime to = parseDate(employeesSortedByFromDateAsc.get(i).getDateTo());
                    for (int j = i+1; j < employeesSortedByFromDateAsc.size(); j++) {
                        LocalDateTime from = parseDate(employeesSortedByFromDateAsc.get(j).getDateFrom());
                        if (to.isAfter(from)){
                            long duration = ChronoUnit.DAYS.between(from , to);
                            if (duration > maxDuration){
                                maxDuration = duration;
                                e1 = employeesSortedByFromDateAsc.get(i);
                                e2 = employeesSortedByFromDateAsc.get(j);
                            }
                        }
                    }
                }
            }
        }
        if (grid.getColumns().size() == 0) {
            grid.addColumn(row -> row[0]).setHeader("FileName");
            grid.addColumn(row -> row[1]).setHeader("DateTime");
            grid.addColumn(row -> row[2]).setHeader("EmplID#1");
            grid.addColumn(row -> row[3]).setHeader("EmplID#2");
            grid.addColumn(row -> row[4]).setHeader("ProjectID");
            grid.addColumn(row -> row[5]).setHeader("DaysWorkedTogether");
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String[] result = {
                fileName ,
                LocalDateTime.now()
                        .format(dtf) ,
                e1.getId() ,
                e2.getId() ,
                e1.getProjectId() ,
                String.valueOf(maxDuration)
        };
        String key = request.getRemoteAddr();
        if (!DB.containsKey(key)){
            DB.put(key , new TreeSet<>((o1, o2) -> LocalDateTime.parse(o2[1] ,dtf)
                                    .compareTo(LocalDateTime.parse(o1[1] , dtf))));
            DB.get(key).add(result);
        }
        else {
            DB.get(key).add(result);
        }
        Set<String[]> resultSet = DB.get(key);
        grid.setItems(resultSet);
    }

    public static LocalDateTime parseDate(String date){
            DateTimeFormatter dtfInput = new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()// For case-insensitive parsing
                    .appendPattern("[d-M-uuuu[ H[:m[:s]]]]")
                    .appendPattern("[uuuu-M-d[ H[:m[:s]]]]")
                    .appendPattern("[uuuu/M/d[ H[:m[:s]]]]")
                    .appendPattern("[d/M/uuuu[ H[:m[:s]]]]")
                    .appendPattern("[d-MMM-uuuu[ H[:m[:s[.SSSSSS]]]]]")
                    .appendPattern("[d-MMM-uu[ H[:m[:s[.SSSSSS]]]]]")
                    .appendPattern("[uuuu-MMM-d[ H[:m[:s[.SSSSSS]]]]]")
                    .appendPattern("[uu-MMM-d[ H[:m[:s[.SSSSSS]]]]]")
                    .appendPattern("[MMMM d,uuuu[ H[:m[:s[.SSSSSS]]]]]")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                    .toFormatter(Locale.ENGLISH);
            return LocalDateTime.parse(date , dtfInput);
    }

//    private void validateEmployee(Employee e){
//        int userId = Integer.parseInt(e.getId());
//        if (userId < 1){
//            throw new BadRequestException("Invalid id");
//        }
//        int projectId = Integer.parseInt(e.getProjectId());
//        if (projectId < 1){
//            throw new BadRequestException()
//        }
//
//    }
}
