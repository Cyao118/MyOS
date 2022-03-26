package com.os;

import java.io.IOException;

class PCB{
		  public PageRegist page_register;
		  int prosize;//进程大小
		  int ProID;//进程编号
		  int Priority;//进程优先级
		  int InTimes;//进入时间
		  int EndTimes;//结束时间
		  int PSW;//0运行态，1就绪态，2阻塞态，3阻塞挂起态，4就绪挂起态
		  int RunTimes;//运行时间
		  int blocktimes;
		  int TurnTimes;
		  int createtime;
		  int instructID;  //当前运行指令ID
	
		  int PC;
		  int IR;
		  int RqNum;
		  int RqTimes;
		  int BqNum;
		  int BqTimes;
		  boolean use;
		  boolean end;
		  public PCB() {
				ProID = -1;
				Priority = -1;
				InTimes = -1;
				EndTimes = -1;
				PSW = -1;
				RunTimes = -1;
				TurnTimes = -1;
				//InstrucNum = -1;
				blocktimes=-1;
				PC = -1;
				IR = -1;
				RqNum = -1;
				RqTimes = -1;
				BqNum = -1;
				BqTimes = -1;
				instructID=-1;
				page_register=new PageRegist();
				use = false;
				end=true;
			}
		  
				public void set_ID(int ID) {
					ProID = ID;
				}
				public void set_Priority(int priority) {
					Priority = priority;
				}
				public void set_InTimes(int intime) {
					InTimes = intime;
				}
				public void set_EndTimes(int endtimes) {
					EndTimes = endtimes;
				}
				public void set_PSW(int psw) {
					PSW = psw;
				}
				//public void set_InstrucNum(int num) {
					//InstrucNum = num;
				//}
				
				public void set_PC(int pc) {
					PC = pc;
				}
				public void set_IR(int ir) {
					IR = ir;
				}
				public void set_RqNum(int rqnum) {
					RqNum = rqnum;
				}
				public void set_RqTimes(int rqtimes) {
					RqTimes = rqtimes;
				}
				public void set_BqNum(int bqnum) {
					BqNum = bqnum;
				}
				public void set_Bqtimes(int bqtimes) {
					BqTimes = bqtimes;
				}
				public void set_end(boolean s) {
					end = s;
				}
				public void set_use(boolean s) {
					use = s;
				}
				public boolean get_end() {
					return end;
				}
				public boolean get_use() {
					return use;
				}
				public int get_ID() {
					return ProID;
				}
				public int get_Priority() {
					return Priority;
				}
				public int get_InTimes() {
					return InTimes;
				}
				public int get_EndTimes() {
					return EndTimes;
				}
				public int get_PSW() {
					return PSW;
				}
				public int get_PC() {
					return PC;
				}
				public int get_IR() {
					return IR;
				}
				public int get_RqNum() {
					return RqNum;
				}
				public int get_RqTimes() {
					return RqTimes;
				}
				public int get_BqNum() {
					return BqNum;
				}
				public int get_Bqtimes() {
					return BqTimes;
				}
				//public int get_InstrucNum() {
					//return InstrucNum;
				//}
	}
public class Process{
	 
      public int JobID;
      public int d1;//资源1
      public int nowd1;//已获得资源数
      public int d2;
      public int nowd2;
	  public int InstrucNum;//指令数目
      public Instruct[] Ir;  //代码段具体内容指令数组
      public PCB pcb;
      public Process() {
    	  pcb=new PCB();
    	  d1=0;
    	  nowd1=0;
    	  d2=0;
    	  nowd2=0;
    	  Ir=null;
      }
      public void ProcessCreate(JCB job, int n) throws IOException {//进程创建原语
  		this.JobID =job.jobid;
  		this.pcb.ProID=n;//n
  		this.pcb.set_Priority(job.priority);
  		this.pcb.createtime = management.nowTime;  //进程创建时间
		this.pcb.RunTimes = 0;

	    //代码段存放的信息
	  		this.pcb.instructID= job.psw;  //当前运行指令ID
	  		this.InstrucNum = job.instructNum;  //指令数目
	  		if(this.InstrucNum % 4 == 0)
	  			this.pcb.prosize = this.InstrucNum / 4 ;  //进程有多少页,初定每页4条指令，向上取整;
	  		else
	  			this.pcb.prosize = this.InstrucNum / 4 + 1;
	  		this.Ir = new Instruct[InstrucNum];  //具体内容的指令数组
	  		for(int i = 1; i <= this.InstrucNum; i++) {  //确定n条指令的成员，指令编号从1开始
	  			this.Ir[i - 1] = new Instruct();
	  			this.Ir[i - 1].setir(i, job.IR[i - 1].get_State(),job.IR[i - 1].getL_Address(),job.IR[i - 1].getRunedtime());//指令编号，指令状态，指令地址

	  		}
	  		this.pcb.blocktimes=0;
	  		this.d1=job.d1;
	  		this.d2=job.d2;
	  		this.nowd1 = 0;
			this.nowd2 = 0;
			//为进程分配内存空间-------分配指令所占页面数
			//页表的长度不包括页表本身所在的物理块那一页表项
			int phy = -1;  //获得的物理块号
			int j = 0;
			if(this.pcb.prosize>3) {
				for (; j < 3; j++) {  //一开始进程只分配三页
					if ((phy = Memory.allocateSpace()) != -1) {  //内存中找到空闲页，可以分配
						if (j == 0) {  //此时是为页表分配空间的时候，将页表的物理块号提前记下
							this.pcb.page_register.pageAddress = phy;  //页表所在内存的物理块号
						}
						Memory.modifyBlock(this, j, phy, true);  //修改物理块号信息
					}
				}
				for (; j < this.pcb.prosize; j++) {  //剩余页面放入外存交换区
					management.swap.allocate(this, j); //第proID号进程的第J号页面送入交换区
					Write_Frame.one.textArea[0].append("第" + j + "页进入外存交换区！\n");
				}
			}
			else
			  {
				  for (; j < this.pcb.prosize; j++) {  //一开始进程只分配三页
					  if ((phy = Memory.allocateSpace()) != -1) {  //内存中找到空闲页，可以分配
						  if (j == 0) {  //此时是为页表分配空间的时候，将页表的物理块号提前记下
							  this.pcb.page_register.pageAddress = phy;  //页表所在内存的物理块号
						  }
						  Memory.modifyBlock(this, j, phy, true);  //修改物理块号信息
					  }
				  }
			  }
			
  		//使用形式a=processCreate(a);
  		//return a;
  	}
  	public void ProcessCancel() throws IOException {//进程撤销原语

		//从PCB表中删除该进程
		for(int i = 0; i <Thequeue.pcb_table.size(); i++) {
			if(Thequeue.pcb_table.get(i).pcb.ProID == this.pcb.ProID) {
				Thequeue.pcb_table.remove(i);  //从PCB表删除当前进程
				break;
			}
		}
		
		//撤销占用的资源！
		Write_Frame.one.textArea[0].append("进程" + this.pcb.ProID + "(作业" +this.JobID + ")释放" + this.nowd1 + "个1号资源," + this.nowd2 + "个2号资源！\n");
		management.deviceTable[0].addValue(this.pcb.ProID,this.nowd1,1);
		management.deviceTable[1].addValue(this.pcb.ProID,this.nowd2,2);
		//Control.deviceTable[2].addValue(this.pcb.ProID,this.nowd3,3);
		
		//释放内存空间
		Memory.realeaseSpace(this);		
		
		//撤销外存交换区的信息
		Swap.deletePro(this.pcb.ProID);
	}
  	
  	public void ProcessBlock() {//进程阻塞原语
  		Thequeue one=new Thequeue();
  		one.In_block(this);

  	}
  	/*public void ProcessWakeUp() {//进程唤醒原语
  		Out_block();
  		In_ready(pcb_table[doing_PCB.get_ID()]);
  	}*/
      
}
 


