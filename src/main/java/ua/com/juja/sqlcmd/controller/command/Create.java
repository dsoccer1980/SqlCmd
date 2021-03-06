package ua.com.juja.sqlcmd.controller.command;

import ua.com.juja.sqlcmd.model.DatabaseManager;
import ua.com.juja.sqlcmd.view.View;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Create implements Command {

    private DatabaseManager manager;
    private View view;

    public Create(DatabaseManager manager, View view) {
        this.manager = manager;
        this.view = view;
    }

    @Override
    public boolean canProcess(String command) {
        return command.startsWith("create|");
    }

    @Override
    public void process(String command) {
        String[] data = command.split("\\|");

        if (data.length < 3) {
            throw new IllegalArgumentException(String.format("Формат команды " +
                    "'create|tableName|column1|column2|...|columnN', а ты прислал: '%s'", command));
        }

        String tableName = data[1];
        List<String> columnList = new ArrayList<>();
        for (int index = 2; index < data.length; index++) {
            columnList.add(data[index]);
        }

        try {
            if (manager.isTableExists(tableName)) {
                throw new IllegalArgumentException(String.format("Таблица %s уже существует", tableName));
            }
            manager.create(tableName, columnList);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        view.write(String.format("Таблица %s была успешно создана.", tableName));
    }

}
