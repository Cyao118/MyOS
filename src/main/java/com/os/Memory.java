package com.os;

import com.sun.org.apache.bcel.internal.generic.SWAP;

import java.io.*;
import java.util.*;

public class Memory {
    Block []InMemory=new Block[32];

	//基本物理信息
	public int memSpace;  //内存空间大小
	public int sysSpace;  //系统区大小
	public int userSpace;  //用户区大小
	public int blockNum;  //用户区物理块数
	public static int blockRemain;  //用户区空闲块数
	//内存管理信息
	public static Block[] block;  //内存物理块分配表 ,设为数组，因为内存中的物理块数是固定的
	
	public Memory() throws IOException {  //内存初始化
		memSpace = 32;
		sysSpace = 8;
		userSpace = 24;  //单位为页
		blockNum = 32;
		blockRemain = 24;
		block = new Block[32];
		int i = 0;
		for(; i < sysSpace; i++) {  //初始化前8个物理块，即系统区
			block[i] = new Block();
		}
		for(; i < memSpace; i++) {  //用户区的24个创建文件，初始为空
			block[i] = new Block();
		}
	}
	
	public static int isallocate() {  //判断当前是否有空闲页面
		
		return blockRemain;  //返回剩余的物理块数
	}

	public static int allocateSpace() {  //返回分配的物理号
		
		for(int i = 8; i < 32; i++) {  //总共有24个用户区的物理块
			if(block[i].get_Blockuse() == 0)   //当前物理块空闲,将当前物理块分给进程
			{
				block[i].set_Blockuse(1);
				blockRemain--;
				return i;
			}
		}
		return -1;
	}
	
	public static void modifyBlock(Process p,int j,int b, boolean flag) throws IOException {  //修改物理块信息
		
		//这个修改是直接将装入页的信息赋值到内存中的

		block[b].BlockID= b;  //物理块号
		block[b].proid = p.pcb.ProID;  //该物理块中存放的第几号进程
		block[b].status= j + 1;  //是该进程的第几页
		//state-1就是存放的页号！！
		block[b].count = 1;
		block[b].irnum = 0;  //这里要将指令的条数清零，因为在进行页面替换的时候，当前物理块的指令数为上一个物理块的指令数，会累加，会出错
		if(p.pcb.page_register.pageAddress == b||p.pcb.page_register.pageAddress == -1) {
			p.pcb.page_register.pageAddress = b;
			common.proresAppend("进程"+p.pcb.ProID+"页表地址为物理内存第"+b+"块\n");
		}
		Memory.updatePage(p.pcb.page_register.pageAddress ,p.pcb.ProID,j,b);
//		页表加一页

		block[b].pcb = p;  //将进程Process放入内存中？？？？？？？？
		block[b].ir = new Instruct[4];  //指令数组，存放指令
		int m = j * 4;  //每页4条指令，当前页的第一条指令的id-1
		for(int i = 0; i < 4 && ((m + i) < p.InstrucNum); i++) {
			block[b].ir[i] = new Instruct();
			block[b].ir[i].setir(m + i + 1, p.Ir[m + i].get_State(),p.Ir[m + i].L_Address,p.Ir[m + i].time, p.Ir[m + i].getRunedtime());/////更改
			block[b].irnum++;
		}

		//saveBlockFile(b,j,flag,p.pcb.page_register.length);  //将b号物理块的信息保存信息到文件中
	}
	

	
	public static void realeaseSpace(Process p) throws IOException {  //页面释放,释放进程p的内存空间
		
		int b = p.pcb.page_register.pageAddress;  //该进程页表所在的物理块
		int phy = -1;
		for(int i = 0; i < block[b].page.size(); i++) {  //进程页表里面所占物理块清空
			phy = block[b].page.get(i).blockNum;
			Swap.setBlock(block[b]);//数据传到外存
			block[phy].clear();  //真是释放所占用的物理块，即清空物理块内的信息
			//clearBlockFile(phy);  //将清空的信息存入文件
			common.proresAppend("内存第"+phy+"块释放\n");
			blockRemain++;  //可分配的物理块数++
		}
		//清除页表所占的物理块
		block[b].page = new LinkedList<Page>();;
	}
	
	public static void updatePage(int bnum,int proID,int j,int phy) throws IOException {  
		//更新页表信息,将从外存调入的页放入进程的页表，bnum为页表所在的物理块号
		
		Page p = new Page(proID, j, phy, false, 1);  //一个新的页表项，注意访问次数
		
		block[bnum].page.add(p);  //将新的页表项插入页表
		
		//showPage(bnum,block[bnum].page.size());  //显示对页表所在物理块的操作
	}
	
	public void deletePage(int proID, int page) throws IOException {  //页面替换选中一个物理块替换出去的时候，需要将物理块所属进程的页表项删除
		Process p = null;
		int i = 0; int j = 0;
		for(; i < Thequeue.pcb_table.size(); i++) {
			if((p = Thequeue.pcb_table.get(i)).pcb.ProID == proID)
				break;
		}
		int add = p.pcb.page_register.pageAddress;
		for(; j < block[add].page.size(); j++) {
			if(block[add].page.get(j).pageNum == page) {
				block[add].page.remove(j);  //删除页表项
				block[add].count++;  //访问页表，次数++
				Thequeue.pcb_table.set(i, p);
				break;
			}
		}
		//showPage(add,length);
	}
	
	public void readIR(int add) {  //读取指令，传进来的是物理地址
		block[add >> 2].count++;  //add>>2是物理块号。访问次数加1
	}
	
	public static  int searchPage(int pageblock,int pageNum) {  //在pageBlock物理块中存放页表，查找页表的信息
	
		Page p=new Page();
		for(int i = 0; i < block[pageblock].page.size(); i++) {  //遍历页表项
			
			if(( p = block[pageblock].page.get(i) ).pageNum == pageNum) {  //找到
				block[pageblock].count++;  //访问页表所在物理块的次数加1
				block[pageblock].page.set(i, p);
				return p.blockNum;  //返回物理块号
			}
		}
		return -1;  //未找到返回-1
	}
	
	public int LRU() {  //页框替换算法
		
		int min = 0;
		for(int i = 8; i < 32; i++) {  //找到第一个指令页或数据页作为最小的
			if(block[i].page.size()==0) {  //进程的指令和数据段页面替换，有页表的页不可以！
				min = i;
				break;
			}
		}
		for(int j = min + 1; j < 32; j++) {
			if(block[min].count > block[j].count && block[j].page.size()==0)  //有页表的页不可以！
				min = j;
		}
		return min;
	}
	
    
}
