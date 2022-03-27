package com.os;

import jdk.nashorn.internal.runtime.regexp.JoniRegExp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.os.management.jobTable;
import static com.os.management.mem;
import static com.os.management.nowTime;
import static com.os.management.pcblength;
/**
 * 作业请求线程
 * @Description
 */
public class JobSheduling extends Thread {
    public JobSheduling(){

    }
    public static volatile boolean stopHS= false;
    public static void StopMe() {
        stopHS=true;
    }
    public static void ReStartMe() {
        stopHS=false;
    }

    public void run(){
        try{
            while(true){
                if(!stopHS) {

                    JCB job = null;
                    while(nowTime%5==0&&mem.isallocate() >= 4 && jobTable.size() > 0) {  //后备作业队列中有作业且内存空间足够
                        //PCB、页表、数据段、堆栈段
                        job = jobSchedule();  //从作业表中选出下一个进入内存的作业(同时删除作业队列中的作业)
                        if(job == null) {
                            common.proresAppend("等待作业到达\n");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            break;//作业未到达

                        }
                        else {
                            common.proresAppend("发生高级调度\n" + job.jobid + "号作业被创建为进程\n");
                            try {
                                File fp = new File("Process.txt");
                                BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
                                output.write("发生高级调度\n" + job.jobid + "号作业被创建为进程\n");
                                output.close();
                            } catch (IOException e2) {
                                // TODO Auto-generated catch block
                                e2.printStackTrace();
                            }
                            Process p = new Process();  //申请空白PCB，真正的PCB空间
                            try {
                                p.ProcessCreate(job, ++pcblength);
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }  //进程创建
                            try {
                                saveProcess(p);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Thequeue.ready.add(p);  //进程进入就绪队列
                            Thequeue.pcb_table.add(p);//进程放入系统pcb表
                            job = null;  //作业已经创建完毕，释放
                            common.proresAppend("进程" + p.pcb.ProID + "(作业" + p.JobID + ")创建\n");

                        }

                    }
                    //若无法创建进程，输出原因
                    if(nowTime%5==0&&mem.isallocate() >= 4 &&jobTable.size() <= 0) {
                        common.proresAppend("后备作业队列为空！\n");
                        try {
                            File fp = new File("Process.txt");
                            BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
                            output.write("后备作业队列为空！\n");
                            output.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if(nowTime%5==0&&mem.isallocate() < 4 && jobTable.size() > 0) {
                        common.proresAppend("内存空间不足！\n");
                        try {
                            File fp = new File("Process.txt");
                            BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
                            output.write("内存空间不足！\n");
                            output.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }
        finally {
        }
    }
    public JCB jobSchedule() {  //高级调度算法，返回一个作业,优先级数越小,优先级越高//先来先服务加优先级
        JCB b = null;
        int min = 0;
        for(int i = 0; i < jobTable.size(); i++) {
            if(jobTable.get(i).arriveTime <= nowTime) {
                //当前优先级数字小且该作业已经创建
                min = i;break;
            }
        }
        if(min == 0) {  //选择的作业未改变是默认的第一个，但是要检测第一个的作业到达时间
            if(jobTable.get(min).arriveTime > nowTime)  //作业未到达
                return null;
        }
        b = jobTable.get(min);
        jobTable.remove(min);  //获得并删除
        return b;
    }
    public void saveProcess(Process p) throws IOException {
        File file = new File("allprocess.txt");
        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(String.valueOf(p.pcb.ProID) + "\t" +
                String.valueOf(p.pcb.Priority) + "\t" +
                String.valueOf(p.pcb.createtime) + "\t" +
                String.valueOf(p.d1) + "\t" +
                String.valueOf(p.d2) + "\t" +
                String.valueOf(p.InstrucNum) + "\t");

        bw.write(String.valueOf(p.Ir[0].Instruct_ID) + "\t" +
                String.valueOf(p.Ir[0].Instruct_State) + "\t" +
                String.valueOf(p.Ir[0].time) + "\t" +
                String.valueOf(p.Ir[0].L_Address) + "\r\n");
        for(int i = 1; i < p.InstrucNum; i++) {
            bw.write("\t\t\t\t\t\t" + String.valueOf(p.Ir[i].Instruct_ID) + "\t" +
                    String.valueOf(p.Ir[i].Instruct_State) + "\t" +
                    String.valueOf(p.Ir[i].time) + "\t" +
                    String.valueOf(p.Ir[i].L_Address) + "\r\n");
        }
        bw.close();
    }
}
