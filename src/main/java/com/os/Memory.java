package com.os;

import java.io.*;
import java.util.*;

public class Memory {
    Block []InMemory=new Block[64];

	//基本物理信息
	public int memSpace;  //内存空间大小
	public int sysSpace;  //系统区大小
	public int userSpace;  //用户区大小
	public int blockNum;  //用户区物理块数
	public static int blockRemain;  //用户区空闲块数
	//内存管理信息
	public static Block[] block;  //内存物理块分配表 ,设为数组，因为内存中的物理块数是固定的
	
	public Memory() throws IOException {  //内存初始化
		memSpace = 64;
		sysSpace = 16;
		userSpace = 48;  //单位为页
		blockNum = 64;
		blockRemain = 48;
		block = new Block[64];
		int i = 0;
		for(; i < sysSpace; i++) {  //初始化前16个物理块，即系统区
			block[i] = new Block();
		}
		for(; i < memSpace; i++) {  //用户区的48个创建文件，初始为空
			block[i] = new Block();
		}
	}
	
	public static int isallocate() {  //判断当前是否有空闲页面
		
		return blockRemain;  //返回剩余的物理块数
	}

	public static int allocateSpace() {  //返回分配的物理号
		
		for(int i = 16; i < 64; i++) {  //总共有48个用户区的物理块
			if(block[i].status == 0)   //当前物理块空闲,将当前物理块分给进程
				return i;
		}
		return -1;
	}
	
	public static void modifyBlock(Process p,int j,int b, boolean flag) throws IOException {  //修改物理块信息
		
		//这个修改是直接将装入页的信息赋值到内存中的
		
		if(flag)  //flag为真，为分配一个新的空闲的物理块，需要减；flag为假，是进行物理块替换的，不需要减
			blockRemain--;//分配用户区
		block[b].BlockID= b;  //物理块号
		block[b].proid = p.pcb.ProID;  //该物理块中存放的第几号进程
		block[b].status= j + 1;  //是该进程的第几页
		//state-1就是存放的页号！！
		block[b].count = 1;
		block[b].irnum = 0;  //这里要将指令的条数清零，因为在进行页面替换的时候，当前物理块的指令数为上一个物理块的指令数，会累加，会出错
		switch(block[b].status) {
			case 1:  //页表段,初始化页表
				block[b].page = new LinkedList<Page>();
				break;
			case 2:  //控制段
				//block[b].pcb = new Process();
				block[b].pcb = p;  //将进程Process放入内存中？？？？？？？？
				break;
			case 3:  //数据段
				block[b].data = p.data;  //存放数据段的数据（一个）
				break;
			case 4:  //堆栈段
				block[b].stack = p.stack;  //存放堆栈段的数据（一个）
				break;
			default:  //代码段 >5
				block[b].ir = new Instruct[4];  //指令数组，存放指令
				int m = (j - 4) * 4;  //每页4条指令，当前页的第一条指令的id-1
				for(int i = 0; i < 4 && ((m + i) < p.InstrucNum); i++) {
					block[b].ir[i] = new Instruct();
					block[b].ir[i].setir(m + i + 1, p.Ir[m + i].get_State(),p.Ir[m + i].L_Address, p.Ir[m + i].getRunedtime());/////更改
					block[b].irnum++;
				}
				break;
		}
		//saveBlockFile(b,j,flag,p.pcb.page_register.length);  //将b号物理块的信息保存信息到文件中
	}
	

	
	public static void realeaseSpace(Process p) throws IOException {  //页面释放,释放进程p的内存空间
		
		int b = p.pcb.page_register.pageAddress;  //该进程页表所在的物理块
		int phy = -1;
		for(int i = 0; i < p.pcb.page_register.length; i++) {  //进程页表里面所占物理块清空
			phy = block[b].page.get(i).blockNum;
			
			block[phy].clear();  //真是释放所占用的物理块，即清空物理块内的信息
			//clearBlockFile(phy);  //将清空的信息存入文件
			blockRemain++;  //可分配的物理块数++
		}
		//清除页表所占的物理块
		block[b].clear();
		//clearBlockFile(b);
		blockRemain++;  //这里忘了将页表所占的物理块清除，剩余物理块数要加一！
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
		int length = p.pcb.page_register.length;
		int add = p.pcb.page_register.pageAddress;
		for(; j < length; j++) {
			if(block[add].page.get(j).pageNum == page) {
				block[add].page.remove(j);  //删除页表项
				block[add].count++;  //访问页表，次数++
				p.pcb.page_register.length--;  //删除页表项，长度减一
				length--;
				Thequeue.pcb_table.set(i, p);
				break;
			}
		}
		//showPage(add,length);
	}
	
	public Instruct readIR(int add) {  //读取指令，传进来的是物理地址
		
		block[add >> 2].count++;  //add>>2是物理块号。访问次数加1
		return block[add >> 2].ir[add & 0x00000003];
	}
	
	public static  int searchPage(int pageblock,int length,int pageNum) {  //在pageBlock物理块中存放页表，查找页表的信息
	
		Page p=new Page();
		for(int i = 0; i < length; i++) {  //遍历页表项
			
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
		for(int i = 16; i < 64; i++) {  //找到第一个指令页或数据页作为最小的
			if(block[i].status > 4 | block[i].status == 3) {  //进程的指令和数据段页面替换，其余的管理信息页面不可以！
				min = i;
				break;
			}
		}
		for(int j = min + 1; j < 64; j++) {
			if(block[min].count > block[j].count && (block[j].status > 4 | block[j].status == 3))  //这里应该加上替换出去的页只可以是指令页
				min = j;
		}
		return min;
	}
	
    
}
