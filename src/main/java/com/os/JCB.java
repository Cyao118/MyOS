package com.os;

public class JCB {
	public int jobid;//作业编号
	public int arriveTime;//创建时间
	public int instructNum;  //指令条数
 	public int psw;  //当前将要执行的指令
 	public Instruct[] IR;  //指令数组
 	public int d1;  //设备需多少资源
 	public int d2;  //设备需多少资源
	double aveRoundTime;//带权周转时间
	public int priority;  //作业优先级
	public short pages_num; //作业所占用的页面数目
	public short in_page_num=0; //该 JCB 所在的页号
	double clock=0;//在时间轮转调度算法中，记录该进程真实服务时间已经用时的时长
	int waitTime;//记录每个进程到达后的等待时间，只用于最高响应比优先调度算法中
	boolean firstTimeTag=false;//在RR算法中标识开始时间是否第一次计算
	
	public JCB() {
		this.jobid= -1;
 		this.priority = -1;
 		this.arriveTime = -1;
 		this.instructNum = -1;
 		this.psw = 1;
 		this.IR = null;
 		this.d1 = 0;
 		this.d2=0;

	}
	
	public void setJob(Process p) {//用于死锁撤销
 		this.jobid = p.pcb.ProID;
 		this.priority = p.pcb.get_Priority();
 		this.arriveTime = management.nowTime+3;
 		this.instructNum = p.InstrucNum;
 		this.psw = 1;
 		this.IR = new Instruct[p.InstrucNum];
 		for(int i = 0; i < p.InstrucNum; i++) {
 			IR[i] = new Instruct();
 			IR[i].setir(p.Ir[i].Instruct_ID, p.Ir[i].Instruct_State,-1);  //地址要重新计算
 		}
 		this.d1 = p.d1;
 		this.d2=p.d2;
 	}
	
}
