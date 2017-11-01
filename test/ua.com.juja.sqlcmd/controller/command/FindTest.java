package ua.com.juja.sqlcmd.controller.command;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import ua.com.juja.sqlcmd.model.DataSet;
import ua.com.juja.sqlcmd.model.DatabaseManager;
import ua.com.juja.sqlcmd.view.View;


import static org.junit.Assert.assertEquals;


public class FindTest {

    private DatabaseManager manager;
    private View view;
    private Command command;

    @Before
    public void setup(){
        manager = mock(DatabaseManager.class);
        view = mock(View.class);
        command = new Find(manager, view);
    }

    @Test
    public void testPrintTableData(){
        //given
        when(manager.getTableColumns("users")).thenReturn(new String[] {"id", "name", "password"});

        DataSet user1 = new DataSet();
        user1.put("id",12);
        user1.put("name","Stiven");
        user1.put("password","*****");
        DataSet user2 = new DataSet();
        user2.put("id",13);
        user2.put("name","Eva");
        user2.put("password","+++++");
        DataSet[] data = new DataSet[]{user1, user2};

        when(manager.getTableData("users")).thenReturn(data);


        //when
        command.process("find|users");

        //then
        shouldPrint("[-----------------, " +
                "|id|name|password|, " +
                "-----------------, " +
                "|12|Stiven|*****|, " +
                "|13|Eva|+++++|, " +
                "-----------------]");

    }

    @Test
    public void testCanProcessFindWithParametersString(){
        //when
        boolean canProcess = command.canProcess("find|users");

        //then
        assertTrue(canProcess);
    }

    @Test
    public void testCantProcessFindWithoutParametersString(){
        //when
        boolean canProcess = command.canProcess("find");

        //then
        assertFalse(canProcess);
    }

    @Test
    public void testCantProcessQweString(){
        //when
        boolean canProcess = command.canProcess("qwe|users");

        //then
        assertFalse(canProcess);
    }

    @Test
    public void testPrintEmptyTableData(){
        //given
        when(manager.getTableColumns("users")).thenReturn(new String[] {"id", "name", "password"});

        when(manager.getTableData("users")).thenReturn(new DataSet[0]);


        //when
        command.process("find|users");

        //then
        shouldPrint("[-----------------," +
                " |id|name|password|, " +
                "-----------------, " +
                "-----------------]");

    }

    @Test
    public void testPrintTableDataWithOneColumn(){
        //given
        when(manager.getTableColumns("test")).thenReturn(new String[] {"id"});

        DataSet user1 = new DataSet();
        user1.put("id",12);
        DataSet user2 = new DataSet();
        user2.put("id",13);
        DataSet[] data = new DataSet[]{user1, user2};

        when(manager.getTableData("test")).thenReturn(data);


        //when
        command.process("find|test");

        //then
        shouldPrint("[-----------------, " +
                "|id|, " +
                "-----------------, " +
                "|12|, " +
                "|13|, " +
                "-----------------]");

    }

    private void shouldPrint(String expected) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(view, atLeastOnce()).write(captor.capture());
        assertEquals(expected,captor.getAllValues().toString());
    }
}
