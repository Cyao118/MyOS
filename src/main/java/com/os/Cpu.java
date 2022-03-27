package com.os;

import java.io.*;

import static com.os.management.cpu;
import static com.os.management.source;

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
		//6是P(full),7是V(full),8是P(printers),9是V(printers)，10死锁P2，11死锁V2,12死锁P3，13死锁V3,
		//返回0，指令正常执行完；返回1，当前进程阻塞,返回2，当前进程已经进入阻塞队列
		Process p = null;
		//根据不同类型指令进行不同操作
			if(IR.get_State()==0) {
				cpu.pcb.Ir[cpu.PC - 1].runedtime++;
				cpu.IR.runedtime++;
				cpu.PC++;
				return 0;
				//普通指令函数
			}

			if(IR.get_State()==1) {  //普通指令函数
				if(cpu.pcb.Ir[cpu.PC-1].time-cpu.pcb.Ir[cpu.PC-1].runedtime>1) {
					cpu.IR.runedtime++;
					cpu.pcb.Ir[cpu.PC - 1].runedtime++;
				}
				else {
					cpu.pcb.Ir[cpu.PC - 1].runedtime++;
					cpu.IR.runedtime++;
					cpu.PC++;
				}
				return 0;
			}
			if(IR.get_State()==2) {
				//死锁P1指令
				this.stutas = 1;  //系统调用
				if(source.PK(cpu.pcb))
					common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请键盘成功！\n");
				else
					common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请键盘失败！\n");
				IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
				common.proresAppend("进程"+cpu.pcb.pcb.ProID+"进入键盘阻塞队列！\n");
				return 2;
			}
	  if(IR.get_State()==3) {
		  //死锁P1指令
		  this.stutas = 1;  //系统调用
		  if(source.PS(cpu.pcb))
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请屏幕成功！\n");
		  else
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请屏幕失败！\n");
		  IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
		  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"进入屏幕阻塞队列！\n");
		  return 2;
	  }

	  if(IR.get_State()==4) {
		  //死锁P1指令
		  this.stutas = 1;  //系统调用
		  if(source.PR(cpu.pcb))
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请读磁盘成功！\n");
		  else
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请读磁盘失败！\n");
		  IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
		  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"进入读磁盘阻塞队列！\n");
		  return 2;
	  }

	  if(IR.get_State()==5) {
		  //死锁P1指令
		  this.stutas = 1;  //系统调用
		  if(source.PW(cpu.pcb))
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请写磁盘成功！\n");
		  else
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请写磁盘失败！\n");
		  IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
		  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"进入写磁盘阻塞队列！\n");
		  return 2;
	  }

	  if(IR.get_State()==6) {
		  //死锁P1指令
		  this.stutas = 1;  //系统调用
		  if(source.PP(cpu.pcb))
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请打印机成功！\n");
		  else
			  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"申请打印机失败！\n");
		  IR.time = 0;  //指令时间置为0，为了在统计系统的时间的时候证明没有执行当前指令
		  common.proresAppend("进程"+cpu.pcb.pcb.ProID+"进入打印机阻塞队列！\n");
		  return 2;
	  }

		return 0;		
	}
}
