package com.os;

import java.io.*;
import java.util.*;

class SwapBlock {  //交换区模拟外存缓冲区
	
	public int proID;  //该物理块中存放的第几号进程
	public int state;  //物理块中存放的类型：0空闲，1页表，2控制段，3数据，4堆栈，5代码（指令）
	public Instruct[] ir;  //指令数组，存放指令
	public int irnum;  //存放的指令条数
	
	public SwapBlock() {
		this.proID = -1;
		this.state = 0;
		this.ir = null;
		irnum = -1;
	}
	//进程的j号页放入外存
	public void setSwap(Process p, int j) throws IOException {  
		
		this.proID = p.pcb.ProID;  //该物理块中存放的第几号进程
		this.state =(p.pcb.ProID-1)*30+j+ 1;  //物理块中存放的类型：0空闲，1控制段，2页表，3数据，4堆栈，5代码（指令）
		this.ir = new Instruct[4];  //指令数组，存放指令
		this.irnum = 0;
		int m = (j - 4) * 4;//定位指令第几页：已出现几条指令
		for(int i = 0; i < 4 && ((m + i) < p.InstrucNum); i++) {
			this.ir[i] = new Instruct();
			this.ir[i].setir(m + i + 1, p.Ir[m + i].get_State(),p.Ir[m + i].L_Address);//add为相对于进程的第几页
			this.irnum++;
		}
	}
	
	public void saveFile(int i,int length) throws IOException {
		File file = new File("Process.txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		if(i == -1) {  //删除这一块外存区的内容
			bw.write("\r\nProcess page Input Memory,Clear this Space\r\n");
		}
		else if(i == 0) {  //进程没有进到内存中的页，分配一块
			bw.write("\r\n为进程分配交换区:\r\n");
			bw.write("该物理块包含的指令有:\r\n");
		}
		else if(i == 1){  //i=1内存换出的物理块
			bw.write("\r\nMemory Page Out,Clear this Space\r\n");
		}
		else {
			bw.write("\r\nProcess lock,Clear this Space\r\n");
		}
		bw.write("Time:" + String.valueOf(management.nowTime) + "\r\n");
		bw.write("ProID: " + String.valueOf(this.proID) + "\r\n");
		bw.write("PageNum: " + String.valueOf(this.state - 1) + "\r\n");
		bw.write("State: Instruct\r\n");
		bw.write("id\tstate\ttimes\r\n");
		for(int k = 0; k < this.irnum; k++)
			bw.write(String.valueOf(this.ir[k].Instruct_ID) + "\t" + String.valueOf(this.ir[k].get_State()) + "\t" + String.valueOf(this.ir[k].time) + "\r\n");
		bw.write("Present Length: " + String.valueOf(length) + "\r\n");
		bw.close();
	}
}

 class Swap{
	
	public static LinkedList<SwapBlock> swapTable;//交换区表
	public static int length;  //记录当前交换区分配的长度
	public File file;
	
	public Swap() throws IOException {  //这里已经创建或者清空了Swap区文件
		swapTable = new LinkedList<SwapBlock>();
		length = 0;
		file = new File("Process.txt");
	
	}
	
	public static void setBlock(Block b) throws IOException {  //把内存b号物理块中的信息保存到外存
		SwapBlock sw = new SwapBlock();  //申请一块外存空间
		sw.proID = b.proid;  //该物理块中存放的第几号进程
		sw.state = b.status;  
		sw.ir = new Instruct[4];  //指令数组，存放指令
		sw.irnum = 0;
		for(int i = 0; i < b.irnum; i++) {   //这是从物理块中直接复制，只有四条指令，不用计算起始指令，直接赋值即可
			sw.ir[i] = new Instruct();
			sw.ir[i].setir(b.ir[i].Instruct_ID, b.ir[i].get_State(),b.ir[i].L_Address);
			sw.irnum++;
		}
		length++;
		swapTable.add(sw);  //将进程的某一页写入外存交换区的物理块中
		sw.saveFile(1,length);  
	}

	public static void allocate(Process p,int j) throws IOException {  //应该将proID号进程的j页内容写进去
		
		SwapBlock sw = new SwapBlock();  //申请一块外存空间
		length++;
		sw.setSwap(p,j);
		sw.saveFile(0,length);  //新分配一块
		swapTable.add(sw);  //将进程的某一页写入外存交换区的物理块中
	}
	
	public static SwapBlock search(int proID, int page) throws IOException {  //找到并删除
		
		SwapBlock sb = null;
		page++;  //page++为保存的state的值
		for(int i = 0; i < swapTable.size(); i++) {
			sb=swapTable.get(i);
			if((swapTable.get(i)).proID == proID && (sb.state-(proID-1)*30) == page) {
				return sb;
			} 
		}
		return null;
	}
	
	public static void deletePro(int proID) throws IOException {  //进程撤销的时候删除进程在外存上的交换区
		
		SwapBlock sb = null;
		for(int i = 0; i < swapTable.size(); i++) {
			if((sb = swapTable.get(i)).proID == proID) {
				swapTable.remove(i);  //找到当前进程在外村上的页所在的位置，删除当前页在外存交换区占用的页
				length--;
				sb.saveFile(-2,length);  //清空当前区
				i--;
			} 
		}
	}
}
public class disk {
   int sector=64;//扇区数
   int track=32;//磁道数
   //设置交换区
   class track{
	   Block[] sector=new Block[64];
	   public track() {  }
   }
   public disk() {
	   
   }
}
