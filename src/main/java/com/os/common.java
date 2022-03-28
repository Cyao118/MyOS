package com.os;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class common {
    public final static int timeslice = 5;
    public final static String user = "19317124";
    public final static int instructTypeNum = 7;
    public final static int firstProNum = 5;
    public final static void proresAppend(String text)
    {
        int num = 0;
        File proresfile = new File("Process_Result.txt");
        try {
            FileWriter proresfw = new FileWriter(proresfile.getAbsoluteFile(), true);
            BufferedWriter proresbw = new BufferedWriter(proresfw);
            proresbw.write(text);
            proresbw.close();
            proresfw.close();
        } catch (IOException e) {
                e.printStackTrace();
        }
        Write_Frame.one.textArea[num].append(text);
    }
}
