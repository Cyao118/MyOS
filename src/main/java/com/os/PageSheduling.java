package com.os;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.os.management.cpu;
import static com.os.management.deviceTable;
import static com.os.management.jobTable;
import static com.os.management.mem;
import static com.os.management.nowTime;
import static com.os.management.swap;

/**
 * 缺页处理线程
 * @Description
 */
public class PageSheduling extends Thread {
    public PageSheduling()
    {

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
//死锁检测
                    int lock = -1;
                    while ((lock = deadlockCheck()) != -1) {  //有死锁进程返回，每次释放一个进程，直到死锁接解除为止
                        try {
                            deadlockRecover(lock);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    //进程唤醒（普通的I/O阻塞队列从阻塞队列中出来，未到时间的减）
                    Process t = null;
                    if (nowTime % 5 == 0 && Thequeue.block.size() != 0) {//每5s检查一次阻塞队列，若存在则唤醒
                        t = Thequeue.block.get(0);         //获取最早进入阻塞队列的指令
                        Thequeue.block.remove(0);  //从阻塞队列出来
                        Thequeue.ready.add(t);  //进入就绪队列
                    }


                    Write_Frame.one.textArea[0].append("当前Device的资源数量：\n");
                    Write_Frame.one.textArea[0].append("Device1: " + deviceTable[0].value + "\tDevice2: " + deviceTable[1].value + "\n");

                    try {
                        showQueue();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }  //显示各个队列的情况
                    //中级调度
                    if (mem.isallocate() < 4 && (Thequeue.block.size() > 0 | Thequeue.ready.size() > 0)) {
                        Process l = null;
                        if (Thequeue.block.size() > 0) {
                            l = Thequeue.block.getFirst();
                            Thequeue.block.remove(l);

                        } else {
                            l = Thequeue.ready.getFirst();
                            Thequeue.ready.remove();
                        }
                        Thequeue.hang.add(l);
                        Thequeue.pcb_table.remove(l);

                        //释放内存空间
                        try {
                            Memory.realeaseSpace(l);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        deviceTable[0].addValue(l.pcb.ProID, l.nowd1, 1);
                        deviceTable[1].addValue(l.pcb.ProID, l.nowd1, 2);

                    }
                    if (mem.isallocate() > 4 && Thequeue.hang.size() > 0) {
                        Process l = null;
                        l = Thequeue.hang.getFirst();
                        Thequeue.hang.remove(l);
                        Thequeue.pcb_table.add(l);
                        Thequeue.hang.remove(l);
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


    public int deadlockCheck() {  //死锁检测：针对于进程中的device设备

        int num = Thequeue.pcb_table.size();  //当前系统内的进程数
        Process m = new Process();

        int[][] allocation = new int[num][2];  //进程已经得到的资源
        int[][] request = new int[num][2];  //进程还需每类资源的数目
        int[] work = new int[2];  //当前系统内每类资源还可以分配的资源数目
        boolean[] finish = new boolean[num];
        int j = 0,k = 0;
        for(; k < num; k++) {
            m = Thequeue.pcb_table.get(k);
            if(m.nowd1<0)
                allocation[k][0] = 0;
            else
                allocation[k][0] = m.nowd1;
            if(m.nowd2<0)
                allocation[k][0] = 0;
            else
                allocation[k][0] = m.nowd2;
            for(j = 0; j < 2; j++) {   //request数组是当前进程要求分配资源确不能够得到，因为每次执行指令只会得到1个资源
                //所以该数组的值不是1就是0
                if(deviceTable[j].searchQueue(m.pcb.ProID))  //当前进程在阻塞队列里面，证明当前进程需要申请资源
                    request[k][j] = 1;
                else
                    request[k][j] = 0;
            }
        }//在阻塞队列里面的进程不一定会死锁，发生死锁的一定在阻塞队列里面
        for(j = 0; j < 2; j++) {
            if(deviceTable[j].value < 0)
                work[j] = 0;  //最小为0
            else
                work[j] = deviceTable[j].value;
        }
        for(j = 0; j <Thequeue.pcb_table.size(); j++) {
            finish[j] = true;
        }

        for(k = 0; k < Thequeue.pcb_table.size(); k++) {
            m = Thequeue.pcb_table.get(k);
            if((deviceTable[0].searchQueue(m.pcb.ProID)&&m.nowd2>0&&deviceTable[1].deviceQueue.size()>0)|(deviceTable[1].searchQueue(m.pcb.ProID)&&m.nowd1>0&&deviceTable[0].deviceQueue.size()>0)) {
                //work[0] += allocation[k][0];
                //work[1] += allocation[k][1];

                finish[k] = false;
            }
            if((request[k][0] <= work[0]) && (request[k][1] <= work[1])) {
                work[0] += allocation[k][0];
                work[1] += allocation[k][1];

            }
            else {
                finish[k] = false;
            }
        }
        for(k = 0; k < Thequeue.pcb_table.size(); k++) {
            if(finish[k] == false)
                return k;
        }
        return -1;
    }

    public void deadlockRecover(int lock) throws IOException {  //死锁恢复,lock号进程放入作业队列

        Write_Frame.one.textArea[0].append("死锁恢复：");

        //从PCB表中删除该进程
        Process l = new Process();
        l=Thequeue.pcb_table.get(lock);
        Thequeue.pcb_table.remove(l);

        //如果要撤销的进程是当前CPU正在执行的进程
        if(cpu.pcb.pcb.ProID == l.pcb.ProID)
            cpu.save();  //CPU现场恢复

        //释放内存空间
        Memory.realeaseSpace(l);

        //普通阻塞队列
        for(int i = 0; i < Thequeue.block.size(); i++) {
            if(Thequeue.block.get(i).pcb.ProID==l.pcb.ProID) {
                Thequeue.block.remove(i);
                i--;
            }
        }

        //就绪队列
        for(int i = 0; i < Thequeue.ready.size(); i++) {
            if(Thequeue.ready.get(i).pcb.ProID == l.pcb.ProID) {
                Thequeue.ready.remove(i);
                i--;
            }
        }

        //清空进程在外存交换区的内容
        swap.deletePro(l.pcb.ProID);

        //撤销占用的资源,找到当前进程所在的资源阻塞队列，从阻塞队列中出队
        Write_Frame.one.textArea[0].append("进程" + l.pcb.ProID + "(作业" + l.JobID + ")释放" + l.nowd1 + "个1号资源！" + l.nowd2 + "个2号资源！\n");
        deviceTable[0].addValue(l.pcb.ProID,l.nowd1,1);
        deviceTable[1].addValue(l.pcb.ProID,l.nowd1,2);
        //this.deviceTable[2].addValue(l.pcb.ProID,l.d3num,3);
        //pcblength--;
        //重新进入后备作业队列
        JCB j = new JCB();
        j.setJob(l);  //进程重新成为作业?????????
        jobTable.add(j);
    }
    public void showQueue() throws IOException {  //队列中进程显示//移动到thequeue中


        File fp = new File("Process.txt");
        BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
        Process t =new Process();
        Write_Frame.one.textArea[1].setText(" ");
        Write_Frame.one.textArea[1].append("系统时间:" + nowTime + "\n");
        showmem();
        output.write("\n系统时间:" + nowTime + "\n");
        Write_Frame.one.textArea[8].setText(" ");
        if(cpu.Isuse) {
            Write_Frame.one.textArea[8].append("运行：\n"+cpu.pcb.pcb.ProID);
            output.write("运行：\n"+cpu.pcb.pcb.ProID);
        }
        if(jobTable.size() >=0) {  //作业后备队列
            Write_Frame.one.textArea[2].setText(" ");
            Write_Frame.one.textArea[2].append("后备作业队列：\nJobID\tPriority\tInTimes\n");
            output.write("后备作业队列：\nJobID\tPriority\tInTimes\n");
            JCB j = null;
            for(int i = 0; i < jobTable.size(); i++) {
                j = jobTable.get(i);
                Write_Frame.one.textArea[2].append(j.jobid + "\t" + j.priority + "\t" + j.arriveTime+"\n");
                output.write(j.jobid + "\t" + j.priority + "\t" + j.arriveTime+"\n");
            }
        }
        if(Thequeue.ready.size() >=0){  //就绪队列
            Write_Frame.one.textArea[3].setText(" ");
            Write_Frame.one.textArea[3].append("就绪队列：\nProID\tjobID\n");
            for(int i = 0; i < Thequeue.ready.size(); i++) {
                t= Thequeue.ready.get(i);
                Write_Frame.one.textArea[3].append(t.pcb.ProID + "\t" +"\n");
            }
        }

        if(Thequeue.block.size() >=0) {  //普通阻塞队列
            Write_Frame.one.textArea[4].setText(" ");
            Write_Frame.one.textArea[4].append("普通阻塞队列：\nProID\tjobID\n");
            for(int i = 0; i < Thequeue.block.size(); i++) {
                t= Thequeue.block.get(i);
                Write_Frame.one.textArea[4].append(t.pcb.ProID + "\t" +"\n");
            }
        }

        for(int i = 0; i < 2; i++) {  //3个资源阻塞队列
            Write_Frame.one.textArea[i+11].setText(" ");
            Write_Frame.one.textArea[i+11].append("资源" + (i+1) + "：ProID\tjobID\n");

            if(deviceTable[i].deviceQueue.size() >0) {
                System.out.println("!!!!!");
                for(int k = 0; k < deviceTable[i].deviceQueue.size(); k++) {
                    t = deviceTable[i].deviceQueue.get(k);
                    Write_Frame.one.textArea[i+11].append(t.pcb.ProID + "\t" + t.JobID+"\n");
                }
            }
        }

        if(management.source.mutexQueue.size() >= 0) {
            Write_Frame.one.textArea[5].setText(" ");
            Write_Frame.one.textArea[5].append("mutex阻塞队列：\nProID\tjobID\n");
            for(int k = 0; k < management.source.mutexQueue.size(); k++) {
                t = management.source.mutexQueue.get(k);
                Write_Frame.one.textArea[5].append(t.pcb.ProID + "\t" +"\n");
            }
        }

        if(management.source.fullQueue.size() >=0) {
            Write_Frame.one.textArea[6].setText(" ");
            Write_Frame.one.textArea[6].append("full阻塞队列：\nProID\tjobID\n");
            for(int k = 0; k < management.source.fullQueue.size(); k++) {
                t = management.source.fullQueue.get(k);
                Write_Frame.one.textArea[6].append(t.pcb.ProID + "\t" +"\n");
            }
        }

        if(management.source.emptyQueue.size() >=0) {
            Write_Frame.one.textArea[7].setText(" ");
            Write_Frame.one.textArea[7].append("empty阻塞队列：\nProID\tjobID\n");
            for(int k = 0; k < management.source.emptyQueue.size(); k++) {
                t = management.source.emptyQueue.get(k);
                Write_Frame.one.textArea[7].append(t.pcb.ProID + "\t" +"\n");
            }
        }
        if(Thequeue.hang.size()>=0) {
            Write_Frame.one.textArea[10].setText(" ");
            Write_Frame.one.textArea[10].append("挂起队列：\nProID\tjobID\n");
            for(int k = 0; k < Thequeue.hang.size(); k++) {
                t = Thequeue.hang.get(k);
                Write_Frame.one.textArea[10].append(t.pcb.ProID + "\t" +"\n");
            }
        }
    }
    public void showmem() {
        int i=0;
        for(;i<64;i++) {
            Write_Frame.one.textArea[1].append(mem.block[i].status+"  ");
            if((i+1)%8==0&&i!=0)
                Write_Frame.one.textArea[1].append("\n");
        }
    }
}
