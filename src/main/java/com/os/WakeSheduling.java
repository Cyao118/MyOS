package com.os;

import static com.os.management.source;
public class WakeSheduling extends Thread {
    //阻塞唤醒队列 将输入输出，磁盘文件操作，屏幕显示和打印都视为阻塞队列唤醒了

    public WakeSheduling(){

    }
    public static volatile boolean stopHS= false;
    public static void StopMe() {
        stopHS=true;
    }
    public static void ReStartMe() {
        stopHS=false;
    }

    public void run() {
        try {
            while (true) {
                if (!stopHS) {

                    try {
                        source.VK();
                        source.VR();
                        source.VW();
                        source.VP();
                        source.VS();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }finally {

        }
    }
}
