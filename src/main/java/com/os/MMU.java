package com.os;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MMU {
   int Logical_address;//逻辑地址作为输入
   int Physical_address;//物理地址作为输出
   public TLB[] tlb;  //固定长度的快表
   public PageRegist page_regist=new PageRegist(); //页表基址寄存器
   public int lengthtlb;  //快表的最大长度
   public int nowlength;  //快表当前的长度
   public MMU() {
	   this.Logical_address=-1;
	   this.Physical_address=-1;
	   this.lengthtlb=3;
	   tlb = new TLB[this.lengthtlb];  //设定快表长度
		for(int i = 0; i < this.lengthtlb; i++) {
			tlb[i] = new TLB();
		}
		nowlength = 0;  //快表当前的长度，初始没有，则设为0，后续增加
   }
   public void set_l(int Logical_address){//设定逻辑地址
	   this.Logical_address= Logical_address;
   }
   public void sey_p(int Physical_address) {//设定物理地址
	   this.Physical_address= Physical_address;
   }
   public int get_l() {//获取逻辑地址
	   return this.Logical_address;
   }
   public int get_p() {//获取物理地址
	   return this.Physical_address;
   }
   //逻辑地址转化为物理地址
   public int l_to_p(int logAddress) throws IOException {
	   Physical_address= -1;  //初始物理地址
	   Logical_address=logAddress;
		int block = -1;  //找到的页号
		
		int pageNum = -1;  //分解得到的页号
		int offset = -1;  //分解得到的偏移
		
		//分解逻辑地址
		
		offset = Logical_address& 0x00000003;  //获得页偏移量 
		pageNum = ((Logical_address) >> 2); // 获得页号
		BufferedWriter output = new BufferedWriter(new FileWriter(new File("otherdate.txt"),true));
		output.write("进程"+management.cpu.pcb.pcb.ProID+"转化前的逻辑地址是："+logAddress+"\n");
		if(( block = searchTLB(pageNum) ) != -1) {  //查找快表找到
			Physical_address = (block << 2) + offset;  //拼接物理地址
			common.proresAppend("查找快表成功\n");
			output.write("查找快表成功\n转化后物理地址是："+Physical_address+"\n");
			//文件输出
			//界面输出
		}
		else {  //快表中未找到,则查页表
			if(( block = searchPage(pageNum) ) == -1) {  //没有找到
				common.proresAppend("查找失败，产生缺页异常\n");
				output.write("查找失败，产生缺页异常\n");
				//文件输出
				//界面输出
				Physical_address= -1;  //物理地址为-1，缺页中断??
			}
			else {  //找到
				common.proresAppend("查找快表失败，查找页表成功\n");
				Physical_address = (block << 2) + offset;  //拼接物理地址
				updateTLB(pageNum,block);  //更新快表信息
				output.write("查找快表失败，查找页表成功\n转化后物理地址是："+Physical_address+"\n");
			}
		}
		output.close();
		return Physical_address;
   }
   //清空快表项
   public void clearTLB() {
	   int i=0;
	   for(;i<this.nowlength;i++) {
		   this.tlb[i].clear();
	   }
   }
 //MMU现场恢复函数
   public void recover(PCB p) {
		page_regist.pageAddress = p.page_register.pageAddress;
		clearTLB();
		nowlength = 0;
	}
   //查找快表	
   public int searchTLB(int pageNum) {
		for(int i = 0; i < this.lengthtlb; i++) {
			if(pageNum == tlb[i].pageNum && tlb[i].isuse) {
				tlb[i].count++;          //访问次数加1
				return tlb[i].blockNum;  //返回查找到的物理页号
			}
		}
		return -1;  //查找失败
	}
   //当查找快表后发现无相关信息，查找页表
   public int searchPage(int pageNum) { 
		return Memory.searchPage(page_regist.pageAddress,pageNum);  //通过页表基址寄存器，查找页表所在的物理块号、长度以及逻辑页号
	}
   //缺页异常
   public int ifright() {
	   return 0;
   }
   //更新快表
   public void updateTLB(int page,int block) {  
		
		if(nowlength < this.lengthtlb) {  //快表未满
			int i = 0;
			for(; i < this.lengthtlb; i++) {
				if(!tlb[i].isuse)         //查找未被使用的快表项
					break;
			}
			tlb[i].blockNum = block;
			tlb[i].pageNum = page;
			tlb[i].count = 1;  //本次访问
			tlb[i].isuse = true;
			nowlength++;  //快表当前长度加一
		}
		else {  //快表满，则选择访问次数最少的替换
			int min = 0;
			for(int i = 1; i < this.lengthtlb; i++) {
				if(tlb[min].count > tlb[i].count)
					min = i;      //查找到对应序号
			}
			tlb[min].blockNum = block;
			tlb[min].pageNum = page;
			tlb[min].count = 1;  //本次访问
			tlb[min].isuse = true;
		}
	}
}

class PageRegist{  //设定页表基址寄存器
	
	public int pageAddress;  //页表在第几个物理块
	
	public PageRegist() {  //初始化
		this.pageAddress = -1;
	}
	public void set_ad(int address) {
		this.pageAddress=address;
	}
	public int get_ad() {
		return this.pageAddress;
	}
}

class TLB{  //设定快表

	public int pageNum;  //页号
	public int blockNum;  //物理块号：是指内存中的物理块号
	public int count;  //访问次数
	public boolean isuse;  //当前快表项是否被占用
	
	public TLB() {//快表初始化
		blockNum = -1;
		pageNum = -1;
		count = 0;
		isuse = false;
	}
	public void set_pageNum(int pageNum) {
		this.pageNum=pageNum;
	}
	public void set_blockNum(int blockNum) {
		this.blockNum=blockNum;
	}
	public void set_count(int count) {
		this.count=count;
	}
	public void set_isuse(boolean isuse) {
		this.isuse=isuse;
	}
	public void clear() {//清空快表
		blockNum = -1;
		pageNum = -1;
		count = 0;
		isuse = false;
	}
}