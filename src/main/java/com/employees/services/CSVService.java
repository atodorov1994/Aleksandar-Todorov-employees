package com.employees.services;

import com.employees.exception.BadRequestException;
import com.employees.exception.NotFoundException;
import com.employees.model.Employee;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.VaadinRequest;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
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
    private static final int CSV_COLUMNS_SIZE = 4;

    @SneakyThrows
    public void showEmployeesWorkingTogether(InputStream inputStream , Grid<String[]> grid , String fileName , VaadinRequest request) {
        var parser = new CSVParserBuilder().withSeparator(',').build();
        var reader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(parser).build();
        validateInput(reader);
        TreeSet<Employee> m = new TreeSet<>(new CsvToBeanBuilder(reader).withType(Employee.class).withVerifier(getEmployeeVerifier()).build().parse());
        List<Employee> empl = new ArrayList<>(m);
        long maxDuration = 0;
        Employee e1 = null;
        Employee e2 = null;
        for (int i = 0; i < empl.size(); i++) {
            Employee empl1 = empl.get(i);
            LocalDateTime to = parseDate(empl1.getDateTo());
            for (int j = i + 1; j < empl.size(); j++) {
                Employee empl2 = empl.get(j);
                if (!empl1.getProjectId().equals(empl2.getProjectId())) {
                    continue;
                }
                LocalDateTime from = parseDate(empl2.getDateFrom());
                long duration = ChronoUnit.DAYS.between(from, to);
                if (duration > maxDuration) {
                    maxDuration = duration;
                    e1 = empl1;
                    e2 = empl2;
                }
            }
        }
        if(e1 == null){
            throw new NotFoundException("No employees that worked together");
        }
        manageGrid(grid);
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

//        Save history for user
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

    private BeanVerifier getEmployeeVerifier() {
        BeanVerifier<Employee> v = e -> {
            if (e.getId().isBlank() || Integer.parseInt(e.getId()) < 1){
                throw new BadRequestException("Invalid 'EmplID' value " + e.getId());
            }
            if (e.getProjectId().isBlank() || Integer.parseInt(e.getProjectId()) < 1){
                throw new BadRequestException("Invalid 'ProjectID' value " + e.getProjectId());
            }
            if (e.getDateFrom().isBlank()){
                throw new BadRequestException("Field 'dateFrom' is mandatory in 'EmplID'=" + e.getId() );
            }
            LocalDate from = parseDate(e.getDateFrom()).toLocalDate();
            if (from.isAfter(LocalDate.now())){
                throw new BadRequestException("Invalid 'fromDate' of 'EmplID'=" + e.getId());
            }
            LocalDate to = parseDate(e.getDateTo()).toLocalDate();
            if (to.isAfter(LocalDate.now())){
                throw new BadRequestException("Invalid 'toDate' of 'EmplID'=" + e.getId());
            }
            if (e.getDateTo().isBlank() || e.getDateTo().equalsIgnoreCase("NULL")){
                e.setDateTo(LocalDate.now().toString());
            }
            if (from.isAfter(to)){
                throw new BadRequestException("Invalid time interval of 'EmplID'=" + e.getId());
            }
            return true;
        };
        return v;
    }

    @SneakyThrows
    private void validateInput(CSVReader reader) {
        if (reader.readNextSilently().length != CSV_COLUMNS_SIZE){
            throw new BadRequestException("Unsupported csv format");
        }
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

    private void manageGrid(Grid<String[]> grid){
        if(grid.getColumns().size() == 1){
            grid.removeAllColumns();
        }
        if (grid.getColumns().size() == 0) {
            grid.addColumn(row -> row[0]).setHeader("FileName");
            grid.addColumn(row -> row[1]).setHeader("DateTime");
            grid.addColumn(row -> row[2]).setHeader("EmplID#1");
            grid.addColumn(row -> row[3]).setHeader("EmplID#2");
            grid.addColumn(row -> row[4]).setHeader("ProjectID");
            grid.addColumn(row -> row[5]).setHeader("DaysWorkedTogether");
        }
    }

}
