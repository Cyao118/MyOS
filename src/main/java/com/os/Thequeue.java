package com.os;

import java.util.*;

public class Thequeue {
	public static LinkedList<Process>ready = new LinkedList<>();//就绪队列
	public static LinkedList<Process>block=new LinkedList<>();//阻塞队列
	public static LinkedList<Process>waitig=new LinkedList<>();//后备作业队列
	public static LinkedList<Process>hang=new LinkedList<>();//挂起队列
	public static LinkedList<Process>hangwaiting=new LinkedList<>();//就绪挂起队列
	//public static LinkedList<PCB>mid=new LinkedList<>();//用于输出队列数据
	public Process doing_PCB;//记录正在操作的进程
	int pcbid=0;//进程编号指针
	public static LinkedList<Process>pcb_table=new LinkedList<>(); //系统PCB表
	public void In_ready(Process doing){//进入就绪队列
		doing_PCB = doing;
		//pcb_table[doing_PCB.get_ID()].set_RqNum(ready.size()+1);
		//pcb_table[doing_PCB.get_ID()].set_PSW(1);
		ready.push(doing_PCB);
		String out;
		out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程进入就绪队列";
		
		//在文件中输出
	}
	public int Out_ready() {//出就绪队列
	    doing_PCB = ready.getFirst();//获取出队列进程ID
		ready.removeFirst();//在队列中删除进程
		return doing_PCB.pcb.get_ID();
		
	}
	public void In_block(Process doing){//进阻塞队列
		doing_PCB = doing;
		//pcb_table[doing_PCB.get_ID()].set_RqNum(block.size()+1);//记录和更改进程信息
		//pcb_table[doing_PCB.get_ID()].set_PSW(2);
		block.push(doing_PCB);
		String out;
		out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程进入阻塞队列";
		
		//在文件中输出
	}
	public int Out_block() {//出阻塞队列
	    doing_PCB = block.getFirst();//获取出队列进程ID
	    block.removeFirst();//在队列中删除进程
	    String out;
	    out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程被从阻塞队列唤醒";
		System.out.println(out); 
		return doing_PCB.pcb.get_ID();
		
	}
	public void In_waiting(Process doing){//进侯备队列
		doing_PCB = doing;
		//pcb_table[doing_PCB.get_ID()].set_RqNum(waiting.size()+1);//记录和更改进程信息
		//pcb_table[doing_PCB.get_ID()].set_PSW(2);
		waitig.push(doing_PCB);
		String out;
		out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程进入侯备队列";
		System.out.println(out); 
		//在文件中输出
	}
	public int Out_waiting() {//出侯备队列
	    doing_PCB = waitig.getFirst();//获取出队列进程ID
	    waitig.removeFirst();//在队列中删除进程
		return doing_PCB.pcb.get_ID();
		
	}
	public void In_hang(Process doing){//进挂起阻塞队列
		doing_PCB = doing;
		//pcb_table[doing_PCB.get_ID()].set_RqNum(hangblock.size()+1);//记录和更改进程信息
		//pcb_table[doing_PCB.get_ID()].set_PSW(3);
		hang.push(doing_PCB);
		String out;
		out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程进入挂起阻塞队列";
		System.out.println(out); 
		//在文件中输出
	}
	public int Out_hang() {//出挂起阻塞队列
	    doing_PCB = hang.getFirst();//获取出队列进程ID
	    hang.removeFirst();//在队列中删除进程
	    String out;
	    out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程被从挂起阻塞队列唤醒";
		System.out.println(out); 
		return doing_PCB.pcb.get_ID();
		
	}
	public void In_hangwaiting(Process doing){//进挂起就绪队列
		doing_PCB = doing;
		//pcb_table[doing_PCB.get_ID()].set_RqNum(hangwaiting.size()+1);//记录和更改进程信息
		//pcb_table[doing_PCB.get_ID()].set_PSW(4);
		hangwaiting.push(doing_PCB);
		String out;
		out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程进入挂起阻塞队列";
		System.out.println(out); 
		//在文件中输出
	}
	public int Out_hangwaiting() {//出挂起就绪队列
	    doing_PCB = hangwaiting.getFirst();//获取出队列进程ID
	    hangwaiting.removeFirst();//在队列中删除进程
	    String out;
	    out= "进程id为"+doing_PCB.pcb.get_ID()+"的进程被从挂起阻塞队列唤醒";
		System.out.println(out); 
		return doing_PCB.pcb.get_ID();
		
	}
	public boolean If_ready() {//判断就绪队列是否为空
		return ready.isEmpty();
	}
	public boolean If_block() {//判断阻塞队列是否为空
		return block.isEmpty();
	}
	public boolean If_waiting() {//判断侯备队列是否为空
		return waitig.isEmpty();
	}
	public boolean If_hangblock() {//判断挂起阻塞队列是否为空
		return hang.isEmpty();
	}
	public boolean If_hangwaiting() {//判断挂起侯备队列是否为空
		return hangwaiting.isEmpty();
	}
	public void set_PCB(PCB a,int num) {
		//pcb_table[num]=a;
	}
	public void creat_PCB(Process a) {
		Process data = new Process();
		data=a;
		//pcb_table[pcbid]=data;
		pcbid++;
		In_ready(data);
	}
	/*public PCB get_PCB(int num) {
		return pcb_table[num];
	}*/
	
	public Process get_doing_PCB() {//获取正在运行的PCB
		return doing_PCB;
	}
	public void get_block() {//获取阻塞队列进程
		int i = 0;
		System.out.println( "当前阻塞队列进程为：" );
		//写入文件
		for (;i < block.size();i++)
		{
			System.out.println(block.get(i)+"\t");
			//写入文件
		}
		System.out.println();
		//文件换行
	}
	public void get_ready() {//获取就绪队列进程
		int i = 0;
		System.out.println( "当前就绪队列进程为：" );
		//写入文件
		for (;i < ready.size();i++)
		{
			System.out.println(ready.get(i)+"\t");
			//写入文件
		}
		System.out.println();
		//文件换行
	}
     
}
