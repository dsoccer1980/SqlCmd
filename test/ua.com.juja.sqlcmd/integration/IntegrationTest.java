package ua.com.juja.sqlcmd.integration;

import org.junit.Before;
import org.junit.Test;
import ua.com.juja.sqlcmd.controller.Main;

import java.io.*;
import java.util.Random;

import static org.junit.Assert.assertEquals;


public class IntegrationTest {
    private ConfigurableInputStream in;
    private ByteArrayOutputStream out;

    @Before
    public void setup() {
        out = new ByteArrayOutputStream();
        in = new ConfigurableInputStream();

        System.setIn(in);
        System.setOut(new PrintStream(out));
    }

    public String getData() {
        try {
            return new String(out.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        }
    }

    @Test
    public void testHelp() {
        //given
        in.add("help");
        in.add("exit");

        //when
        Main.main(new String[0]);

        //then
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                "Существующие комманды:\r\n" +
                "\tconnect|databaseName|userName|password\r\n" +
                "\t\t - подключиться к базе данных, с которой будем работать\r\n" +
                "\ttables\r\n" +
                "\t\t - вывод списка всех таблиц базы данных, к которой подключились\r\n" +
                "\tclear|tableName\r\n" +
                "\t\t - очистка всей таблицы\r\n" +
                "\tinsert|tableName|column1|value1|column2|value2|...|columnN|valueN\r\n" +
                "\t\t - создание записи в таблице\r\n" +
                "\tcreate|tableName|column1|column2|...|columnN\r\n" +
                "\t\t - создание таблицы\r\n" +
                "\tupdate|tableName|column1|value1|column2|value2|...|columnN|valueN\r\n" +
                "\t\t - обновить запись, установив значение column2 = value2,..,columnN = valueN, для которой соблюдается условие column1 = value1\r\n" +
                "\tfind|tableName\r\n" +
                "\t\t - получить содержимое таблицы 'tableName'\r\n" +
                "\tdrop|tableName\r\n" +
                "\t\t - удалить таблицу\r\n" +
                "\tdelete|tableName|column|value\r\n" +
                "\t\t - удалить запись в таблице\r\n" +
                "\thelp\r\n" +
                "\t\t - вывод существующих команд на экран\r\n" +
                "\texit\r\n" +
                "\t\t - выход из программы\r\n" +
                "Введи команду или help для помощи:\r\n" +
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testExit() {
        //given
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testListWithoutConnect() {
        //given
        in.add("list");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                "Вы не можете пользоваться командой 'list', пока не подключитесь с помощью команды connect|databaseName|userName|password\r\n" +
                "Введи команду или help для помощи:\r\n" +
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testFindUserWithoutConnect() {
        //given
        in.add("find|users");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                "Вы не можете пользоваться командой 'find|users', пока не подключитесь с помощью команды connect|databaseName|userName|password\r\n" +
                "Введи команду или help для помощи:\r\n" +
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testUnsupported() {
        //given
        in.add("unsupported");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                "Вы не можете пользоваться командой 'unsupported', пока не подключитесь с помощью команды connect|databaseName|userName|password\r\n" +
                "Введи команду или help для помощи:\r\n" +
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testUnsupportedAfterConnect() {
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("unsupported");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //unsupported
                "Несуществующая команда:unsupported\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testListAfterConnect() {
        //given
        String tableName1 = "test";
        String tableName2 = "test";
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("create|" + tableName1 + "|name");
        in.add("create|" + tableName2 + "|name");
        in.add("tables");
        in.add("exit");

        //when
        Main.main(new String[0]);
        String data = getData();
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Таблица " + tableName1 + " была успешно создана.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Неудача по причине:Таблица " + tableName1 + " уже существует\r\n" +
                "Повтори попытку.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Таблица " + tableName2 + " была успешно создана.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Неудача по причине:Таблица " + tableName2 + " уже существует\r\n" +
                "Повтори попытку.\r\n","");
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //tables
                "[test, users]\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", data);
    }

    @Test
    public void testFindWithoutDataAfterConnect() {

        //given
        String tableName1 = "users";
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("create|" + tableName1 + "|name|password|id");
        in.add("clear|" + tableName1);
        in.add("yes");
        in.add("find|" + tableName1);
        in.add("exit");

        //when
        Main.main(new String[0]);
        String data = getData();
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Таблица " + tableName1 + " была успешно создана.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Неудача по причине:Таблица " + tableName1 + " уже существует\r\n" +
                "Повтори попытку.\r\n","");
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //clear|users
                "Вы уверены, что хотите очистить таблицу: users. yes/no?\r\n" +
                //yes
                "Таблица users была успешно очищена.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //find|users
                "-----------------\r\n" +
                "|name|password|id|\r\n" +
                "-----------------\r\n" +
                "-----------------\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", data);
    }

    @Test
    public void testConnectAfterConnect() {
        //given
        String tableName1 = "test";
        String tableName2 = "users";
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("create|" + tableName1 + "|name");
        in.add("create|" + tableName2 + "|name");
        in.add("tables");
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("tables");
        in.add("exit");

        //when
        Main.main(new String[0]);
        String data = getData();
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Таблица " + tableName1 + " была успешно создана.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Неудача по причине:Таблица " + tableName1 + " уже существует\r\n" +
                "Повтори попытку.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Таблица " + tableName2 + " была успешно создана.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                "Неудача по причине:Таблица " + tableName2 + " уже существует\r\n" +
                "Повтори попытку.\r\n","");
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //tables
                "[test, users]\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //tables
                "[test, users]\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", data);
    }

    @Test
    public void testConnectWithError() {
        //given
        in.add("connect|sqlcmd|");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Неудача по причине:Неверное количество параметров, разделенных знаком '|', ожидается 4, а есть 2\r\n" +
                "Повтори попытку.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testFindAfterConnectWithData() {
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("clear|users");
        in.add("yes");
        in.add("insert|users|id|13|name|Stiven|password|*****");
        in.add("insert|users|id|14|name|Eva|password|+++++");
        in.add("find|users");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //clear|users
                "Вы уверены, что хотите очистить таблицу: users. yes/no?\r\n" +
                //yes
                "Таблица users была успешно очищена.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //insert|users|id|13|name|Stiven|password|*****
                "Запись {names:[id, name, password], values:[13, Stiven, *****]} в таблице 'users' была успешно создана.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //insert|users|id|14|name|Eva|password|+++++
                "Запись {names:[id, name, password], values:[14, Eva, +++++]} в таблице 'users' была успешно создана.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //find|users
                "-----------------\r\n" +
                "|name|password|id|\r\n" +
                "-----------------\r\n" +
                "|Stiven|*****|13|\r\n" +
                "|Eva|+++++|14|\r\n" +
                "-----------------\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testClearWithError() {
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("clear|users|something");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //clear|users|something
                "Неудача по причине:Формат комманды 'clear|tableName', а ты ввел: clear|users|something\r\n" +
                "Повтори попытку.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testClearWithCancel() {
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("clear|users");
        in.add("no");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //clear|users
                "Вы уверены, что хотите очистить таблицу: users. yes/no?\r\n" +
                //no
                "Команда по очистке таблице отменена.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testInsertWithError() {
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("insert|users|something");
        in.add("exit");

        //when
        Main.main(new String[0]);
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //insert|users|something
                "Неудача по причине:Должно быть четное количество параметров в формате 'insert|tableName|column1|value1|column2|value2|...|columnN|valueN', а ты прислал: 'insert|users|something'\r\n" +
                "Повтори попытку.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", getData());
    }

    @Test
    public void testDropTable() {
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("create|test|id");
        in.add("drop|test");
        in.add("yes");
        in.add("exit");

        //when
        Main.main(new String[0]);
        String data = getData();
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                                "Таблица test была успешно создана.\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                               "Неудача по причине:Таблица test уже существует\r\n" +
                                "Повтори попытку.\r\n","");
        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //clear|users
                "Вы уверены, что хотите удалить таблицу: test. yes/no?\r\n" +
                //yes
                "Таблица test была успешно удалена.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", data);
    }

    @Test
    public void testCreateTable() {
       String tableName = "test";
        //given
        in.add("connect|sqlcmd|postgres|postgres");
        in.add("drop|" + tableName);
        in.add("yes");
        in.add("create|" + tableName + "|name|password|id");
        in.add("find|" + tableName);
        in.add("exit");

        //when
        Main.main(new String[0]);
        String data = getData();
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                               "Неудача по причине:Таблицы test не существует\r\n" +
                                "Повтори попытку.\r\n" +
                                "Введи команду или help для помощи:\r\n" +
                                 "Несуществующая команда:yes\r\n","");
        data = data.replaceFirst("Введи команду или help для помощи:\r\n" +
                                "Вы уверены, что хотите удалить таблицу: test. yes/no\\?\r\n" +
                                "Таблица test была успешно удалена.\r\n","");

        assertEquals("Привет юзер!\r\n" +
                "Введи, пожалуйста, имя базы данных, имя пользователя и пароль в формате: connect|database|username|password\r\n" +
                //connect
                "Успех!\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //create|test3|id|name|password
                "Таблица " + tableName + " была успешно создана.\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //find|test3
                "-----------------\r\n" +
                "|name|password|id|\r\n" +
                "-----------------\r\n" +
                "-----------------\r\n" +
                "Введи команду или help для помощи:\r\n" +
                //exit
                "До скорой встречи!\r\n", data);
    }


}
