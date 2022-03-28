package com.os;

import java.io.*;
import java.util.*;

public class Block {
  public static final int Bsize = 512;//物理块大小
  int []Blocksize=new int[Bsize];//物理快内容
  int BlockID;//物理块编号
  public int Blockuse;//0为未占用，1为占用
  public int proid;
  public int status;
  public int data;
  public int stack;
  public LinkedList<Page> page;  //进程的页表
  public Instruct[] ir;
  public Process pcb;  //真正的放一个Process
  public int count;  //访问次数
  public int irnum;  //当前页存放的指令数目
  public File file;   //对应文件指针
  public Block() {
	  this.BlockID=-1;
	  this.proid=-1;
	  this.status=0;
	  this.data=-1;
	  this.page= new LinkedList<Page>();;
	  this.ir=null;
	  this.pcb=null;
	  this.count=0;
	  this.irnum=0;
	  this.file=null;
	  this.Blockuse=0;	  
  }
  public Block(int BlockId,int Blockuse) {
	  this.BlockID=BlockId;
	  this.Blockuse=Blockuse;
  }
  public void set_Blocksize(int Blocksize[]) {//将数据或指令写入物理块
	  int i=0;
	  for(;i<Bsize;i++) {
		  this.Blocksize[i]=Blocksize[i];
	  }
  }
  public void set_BlockID(int id) {//标记物理快号
	  this.BlockID=id;
  }
  public void set_Blockuse(int use) {//标记物理块使用情况
	  this.Blockuse=use;  
  }
  public int get_BlockID() {
	  return this.BlockID;
  }
  public int get_Blockuse() {
	  return this.Blockuse;
  }	
  public int[] get_Block() {  //获得物理块内容
	  return this.Blocksize;
  }
  public void clear() {
	  this.BlockID=-1;
	  this.proid=-1;
	  this.status=0;
	  this.data=-1;
	  this.ir=null;
	  this.pcb=null;
	  this.count=0;
	  this.irnum=0;
	  this.file=null;
	  this.Blockuse=0;
	}

}
