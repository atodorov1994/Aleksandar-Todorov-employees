package com.employees.views.csv;

import com.employees.services.CSVService;
import com.employees.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinRequest;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("CSV")
@Route(value = "employees", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class CSVView extends VerticalLayout {

    @Autowired
    CSVService csvService;

    Grid<String[]> grid = new Grid<>();

    public CSVView() {
        var buffer = new MemoryBuffer();
        var upload = new Upload(buffer);
        upload.addSucceededListener(e -> {
            try {
                csvService.showEmployeesWorkingTogether(buffer.getInputStream(), grid, buffer.getFileName(), VaadinRequest.getCurrent());
            } catch (Exception ex) {
                grid.removeAllColumns();
                if (grid.getColumns().isEmpty()) {
                    grid.addColumn(row -> row[0]).setHeader("Error message");
                }
                String[] s = {ex.getMessage()};
                grid.setItems(s);
            }
        });
        add(grid , upload);
    }

}
