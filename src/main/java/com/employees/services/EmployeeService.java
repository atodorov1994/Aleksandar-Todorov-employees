package com.employees.services;

import com.employees.exception.BadRequestException;
import com.employees.exception.ErrorCode;
import com.employees.exception.NotFoundException;
import com.employees.model.Employee;
import com.employees.model.EmployeesResponse;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class EmployeeService {
    private static final int CSV_COLUMNS_SIZE = 4;

    public EmployeesResponse showEmployeesWorkingTogether(MultipartFile file) throws IOException {

        var parser = new CSVParserBuilder().withSeparator(',').build();
        var reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream())).withCSVParser(parser).build();

        validateInput(reader);

        List<Employee> employees = new CsvToBeanBuilder(reader)
                .withType(Employee.class)
                .withVerifier(getEmployeeVerifier())
                .build()
                .parse();

        TreeSet<Employee> employeesSortedAndDuplicatesRemoved = new TreeSet<>(employees);
        List<Employee> empl = new ArrayList<>(employeesSortedAndDuplicatesRemoved);
        long maxDuration = 0;
        Employee e1 = null;
        Employee e2 = null;

        for (int i = 1; i < empl.size(); i++) {

            Employee empl1 = empl.get(i);
            LocalDateTime to = parseDate(empl1.getDateTo());

            for (int j = i + 1; j < empl.size(); j++) {
                Employee empl2 = empl.get(j);

                if (!empl1.getProjectId().equals(empl2.getProjectId())) {
                    continue;
                }

                LocalDateTime from = parseDate(empl2.getDateFrom());

                if (from.isAfter(to)){
                    continue;
                }

                long duration = ChronoUnit.DAYS.between(from, to);

                if (duration > maxDuration) {
                    maxDuration = duration;
                    e1 = empl1;
                    e2 = empl2;
                }
            }
        }

        if(e1 == null) {
            throw new NotFoundException("No employees that worked together", ErrorCode.NO_EMPLOYEES_WORKED_TOGETHER);
        }

        return new EmployeesResponse(e1, e2, Long.valueOf(e1.getProjectId()), maxDuration);
    }

    private BeanVerifier<Employee> getEmployeeVerifier() {

        return e -> {
            if (e.getId().isBlank() || Integer.parseInt(e.getId()) < 1) {
                throw new BadRequestException("Invalid 'EmplID' value " + e.getId(), ErrorCode.INVALID_PARAM);
            }
            if (e.getProjectId().isBlank() || Integer.parseInt(e.getProjectId()) < 1) {
                throw new BadRequestException("Invalid 'ProjectID' value " + e.getProjectId(), ErrorCode.INVALID_PARAM);
            }
            if (Objects.isNull(e.getDateFrom()) || e.getDateFrom().isBlank()) {
                throw new BadRequestException("Field 'dateFrom' is mandatory in 'EmplID'=" + e.getId(), ErrorCode.INVALID_PARAM);
            }
            LocalDate from = parseDate(e.getDateFrom()).toLocalDate();
            if (from.isAfter(LocalDate.now())) {
                throw new BadRequestException("Invalid 'fromDate' of 'EmplID'=" + e.getId(), ErrorCode.INVALID_PARAM);
            }
            LocalDate to = parseDate(e.getDateTo()).toLocalDate();
            if (to.isAfter(LocalDate.now())) {
                throw new BadRequestException("Invalid 'toDate' of 'EmplID'=" + e.getId(), ErrorCode.INVALID_PARAM);
            }
            if (e.getDateTo().isBlank() || e.getDateTo().equalsIgnoreCase("NULL")) {
                e.setDateTo(LocalDate.now().toString());
            }
            if (from.isAfter(to)) {
                throw new BadRequestException("Invalid time interval of 'EmplID'=" + e.getId(), ErrorCode.INVALID_PARAM);
            }
            return true;
        };
    }

    private void validateInput(CSVReader reader) throws IOException {
        if (reader.readNextSilently().length != CSV_COLUMNS_SIZE){
            throw new BadRequestException("Unsupported csv format", ErrorCode.INVALID_CSV_FORMAT);
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
}
