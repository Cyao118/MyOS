package com.os;

public class Page {//对应一个页表项
	public int pageNum;  //页号
	public int blockNum;  //页框号
	public int ifin;  //状态位,0为未调入，1为调入
	public int proID;  //进程标号
	public int count;  //访问次数
	public boolean isModify;  //是否修改
	public int badd;//外存地址
	
	public Page() {   //初始化
		this.pageNum = -1;
		this.blockNum = -1;
		this.ifin=0;
		this.proID = -1;
		this.count = 0;
		this.isModify = false;
		this.badd=-1;
	}
	
	public Page(int id, int page, int block, boolean modify, int count) {
		this.pageNum = page;
		this.blockNum = block;
		this.ifin=0;
		this.proID = id;
		this.count = count;
		this.isModify = modify;
		this.badd=-1;
	}
}
