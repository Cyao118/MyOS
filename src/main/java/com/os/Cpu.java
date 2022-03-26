package com.os;

import java.io.*;

public class Cpu {//runtime被注释，需要添加时间片和系统时间！！！！！！
  public int PC;//程序寄存器
  public Instruct IR;//指令寄存器
  public int stutas;//CPU当前所处状态  0为用户态，1为内核态
  public MMU mmu;//逻辑地址到物理地址转换
  public int MDR;//地址寄存器
  public int times;//当前进程运行时间
  public Process pcb;//当前运行指令
  public int clocks=0;//系统时间
  public boolean isEnd;  //当前进程是否要结束
  public boolean Isuse;//CPU是否忙碌
  private boolean IsBreak;
  private class kernel{
	    
	    public PCB ProcessCreate()	//进程创建原语
	    {
	    	PCB a=new PCB();
	    	
	    	return a;
	    }
		public void ProcessCancel()	//进程撤销原语
		{
			
		}
		public void ProcessBlock()	//进程阻塞原语
		{
			
		}
		public void ProcessWakeUp()	//进程唤醒原语
		{
			
		}
  }
  public Cpu(){
	  this.PC=-1;
	  this.IR=new Instruct();
	  this.mmu=new MMU();
	  this.MDR=-1;
	  this.stutas=0;
	  this.times=common.timeslice;
	  this.clocks=0;
	  this.pcb=new Process();
	  this.isEnd=true;
	  this.Isuse=false;
  }
  public void In_Break() {
	  this.IsBreak=true;
  }
  public void Out_Break() {
	  this.IsBreak=false;
  }
  public boolean Get_Break() {
	  return IsBreak;
  }
  public void cpu_check() {
	  
  }
  public void save() {  //现场保护
		this.pcb.pcb.instructID = this.PC;  //下一条要执行的指令
		this.pcb.pcb.RunTimes =this.times;  //记录进程在CPU中的运行时间
		//当前进程结束
		this.isEnd = true;
		this.Isuse = false;
		this.stutas = 0;  //CPU变为空闲
	}	
  public void recover(Process p) {   //现场恢复
		this.PC = p.pcb.instructID;  //当前将要运行指令的ID
		this.IR.clear();  //指令寄存器清空
		this.pcb= p;
		this.mmu.recover(p.pcb);  //MMU现场恢复
		this.MDR = -1; 
		this.stutas= 0;  //恢复成初始用户态
		this.times= common.timeslice;  //当前CPU的运行时间为0
		this.isEnd = false;
		this.Isuse=true;

		
	}
  public int Execute() throws IOException {//根据不同指令进行不同操作
		
		//指令执行函数：0正常执行，1死锁P1指令，2死锁V1指令（进程死锁），3普通阻塞，4是P(mutex),5是V(mutex),
		//6是P(full),7是V(full),8是P(empty),9是V(empty)，10死锁P2，11死锁V2,12死锁P3，13死锁V3,
		//返回0，指令正常执行完；返回1，当前进程阻塞,返回2，当前进程已经进入阻塞队列
		Process p = null;
		//根据不同类型指令进行不同操作
			if(IR.get_State()==0)  //普通指令函数   
				return 0; 
				
			if(IR.get_State()==1) {
				//死锁P1指令
				this.stutas = 1;  //系统调用
				if(management.deviceTable[0].P(pcb)) {  //直接找1号资源的分配表
					//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
					pcb.nowd1++;  //进程得到资源，占用资源加1
					Write_Frame.one.textArea[0].append("进程" + this.pcb.pcb.ProID + "(作业" + this.pcb.JobID + ")获得1号资源\n");
					this.stutas = 0;  //系统调用执行完毕
					return 0;
				}
				else {  //当前进程   “已经进入了”  资源的阻塞队列
					IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
					Write_Frame.one.textArea[0].append("进程进入1号资源的阻塞队列！\n");
					//this.state = 0;
					return 2;
				}
			}


			if(IR.get_State()==2) {
				this.stutas = 1;
				if(( p = management.deviceTable[0].V() ) != null ) {  //有进程出阻塞队列
					p.nowd1++;
					Thequeue.ready.add(p);  //直接在这里就进入就绪队列？？？？？？
					Write_Frame.one.textArea[0].append("进程" + p.pcb.ProID + "(作业" + p.JobID + ")被唤醒，进入就绪队列！\n");
				}
				else {  //没有阻塞进程出队
					Write_Frame.one.textArea[0].append("进程" + this.pcb.pcb.ProID + "(作业" + this.pcb.JobID + ")释放1号资源！\n");
				}
				//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
				pcb.nowd1--;  //进程释放一个资源，占用的资源数减1
				this.stutas = 0;
				return 0;
			}
			//死锁V1指令


			if(IR.get_State()==3) {
				pcb.pcb.blocktimes = 50;   //阻塞时间为50
				IR.time = 0;  //指令时间为0
				return 1;  //需要阻塞当前进程
			}//普通阻塞

			if(IR.get_State()==4) {//P(mutex)
				this.stutas = 1;
				if(management.source.PM(pcb)) {  //可以进入
					//runtime -= IR.time;
					Write_Frame.one.textArea[0].append("【P(mutex)】：进入临界区！\n");
					this.stutas = 0;
					return 0;
				}
				else {
					IR.time = 0;
					Write_Frame.one.textArea[0].append("【P(mutex)】:进入阻塞队列\n");
					//this.state = 0;
					return 2;
				}
			}
			if(IR.get_State()==5) { //V(mutex)
				this.stutas = 1;
				if(( p = management.source.VM() ) != null ) {  //有进程出阻塞队列
                    Thequeue.ready.add(p);
					Write_Frame.one.textArea[0].append("【V(mutex)】进程" + p.pcb.ProID + "(作业" + p.JobID + ")被唤醒进入就绪队列！\n");
				}
				//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
				Write_Frame.one.textArea[0].append("【V(mutex)】:退出临界区！\n");
				this.stutas = 0;
				return 0;
			}
			if(IR.get_State()==6) { //P(full)
				this.stutas = 1;
				if(management.source.PF(pcb)) {  //可以进入
					//runtime -= IR.time;
					Write_Frame.one.textArea[0].append("【P(full)】：进入！\n");
					this.stutas = 0;
					return 0;
				}
				else {
					IR.time = 0;
					Write_Frame.one.textArea[0].append("【P(full)】：阻塞！\n");
					//this.state = 0;
					return 2;
				}
			}
			if(IR.get_State()==7) { //V(full)
				this.stutas = 1;
				if(( p = management.source.VF() ) != null ) {  //有进程出阻塞队列
					Thequeue.ready.add(p);  //直接在这里就进入就绪队列？？？？？？
					Write_Frame.one.textArea[0].append("【V(full)】进程" + p.pcb.ProID + "(作业" + p.JobID + ")被唤醒进入就绪队列！\n");
				}
				//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
				Write_Frame.one.textArea[0].append("【V(full)】：full++\n");
				this.stutas = 0;
				return 0;
			}
			if(IR.get_State()==8) { //P(empty)
				this.stutas = 1;
				if(management.source.PE(pcb)) {  //可以进入
					//runtime -= IR.time;
					Write_Frame.one.textArea[0].append("【P(empty)】:empty--\n");
					this.stutas = 0;
					return 0;
				}
				else {
					IR.time = 0;
					Write_Frame.one.textArea[0].append("【P(empty)】:阻塞\n");
					//this.state = 0;
					return 2;
				}
			}
			if(IR.get_State()==9) {  //9是V(empty)
				this.stutas = 1;
				if(( p =management.source.VE() ) != null ) {  //有进程出阻塞队列
					Thequeue.ready.add(p);  //直接在这里就进入就绪队列？？？？？？
					Write_Frame.one.textArea[0].append("【V(empty)】：进程" + p.pcb.ProID + "(作业" + p.JobID + ")被唤醒，进入就绪队列！\n");
				}
				//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
				Write_Frame.one.textArea[0].append("【V(empty)】：empty++\n");
				this.stutas = 0;
				return 0;
			}
			if(IR.get_State()==10) { //死锁P2指令
				this.stutas = 1;
				if(management.deviceTable[1].P(pcb)) {  //直接找2号资源的分配表
					//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
					pcb.nowd2++;  //进程得到资源，占用资源加1
					Write_Frame.one.textArea[0].append("进程" + this.pcb.pcb.ProID + "(作业" + this.pcb.JobID + ")获得2号资源\n");
					this.stutas = 0;
					return 0;
				}
				else {  //当前进程   “已经进入了”  资源的阻塞队列
					IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
					Write_Frame.one.textArea[0].append("进程进入2号资源的阻塞队列！\n");
					//this.state = 0;
					return 2;
				}
			}
			if(IR.get_State()==11) { //死锁V2指令
				this.stutas = 1;
				if(( p = management.deviceTable[1].V() ) != null ) {  //有进程出阻塞队列
					p.nowd2++;
					Thequeue.ready.add(p);  //直接在这里就进入就绪队列？？？？？？
					Write_Frame.one.textArea[0].append("进程" + p.pcb.ProID + "(作业" + p.JobID + ")被唤醒，进入就绪队列！\n");
				}
				else {  //没有阻塞进程出队
					Write_Frame.one.textArea[0].append("进程" + this.pcb.pcb.ProID + "(作业" + this.pcb.JobID + ")释放2号资源！\n");
				}
				//runtime -= IR.time;  //执行当前指令，模拟对资源的操作
				pcb.nowd2--;  //进程释放一个资源，占用的资源数减1
				this.stutas = 0;
				return 0;
			}
		return 0;		
	}
}
