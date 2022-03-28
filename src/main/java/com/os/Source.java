package com.os;

import java.io.IOException;
import java.util.*;

import static com.os.management.cpu;


public class Source {
	public int keyboards;
	public int printers;
	public int screens;
	public int reads;
	public int writes;
	public List<Process> printersQueue;  //打印机阻塞队列
	public List<Process> keyboardsQueue;  //键盘阻塞队列
	public List<Process> screensQueue;  //屏幕阻塞队列
	public List<Process> readsQueue;  //读磁盘阻塞队列
	public List<Process> writesQueue;  //写磁盘阻塞队列

	public Source() {
		printersQueue = new ArrayList<Process>();
		keyboardsQueue = new ArrayList<Process>();
		screensQueue = new ArrayList<Process>();
		readsQueue = new ArrayList<Process>();
		writesQueue = new ArrayList<Process>();
	}
	
	public boolean PP(Process p) {  //生产者P(printers)操作

		if(printers != 0&&printers != p.pcb.ProID) {  //无法得到资源
			printersQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		printersQueue.add(p);
		printers = p.pcb.ProID;
		return true;
	}
	
	public boolean PK(Process p) {  //生产者P(full)操作

		if(keyboards != 0&&keyboards!= p.pcb.ProID) {  //无法得到资源
			keyboardsQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		keyboardsQueue.add(p);
		keyboards= p.pcb.ProID;
		return true;
	}
	
	public boolean PS(Process p) {  //互斥访问P(mutex)

		if(screens != 0&&screens !=p.pcb.ProID) {  //无法得到资源
			screensQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		screensQueue.add(p);
		screens= p.pcb.ProID;
		return true;
	}

	public boolean PR(Process p) {  //互斥访问P(mutex)
		if(reads != 0&&reads!= p.pcb.ProID) {  //无法得到资源
			readsQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		readsQueue.add(p);
		reads= p.pcb.ProID;
		return true;
	}

	public boolean PW(Process p) {  //互斥访问P(mutex)

		if(writes != 0&&writes!= p.pcb.ProID) {  //无法得到资源
			writesQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		writesQueue.add(p);
		writes= p.pcb.ProID;
		return true;
	}

	public Process VS() {  //互斥访问V(mutex)
		if(screens != 0) {
			Process p = screensQueue.get(0);  //阻塞队列队头元素出队
			p.Ir[p.pcb.instructID-1].runedtime++;
			common.proresAppend("进程"+p.pcb.ProID+"使用屏幕"+p.Ir[p.pcb.instructID-1].time+"秒,已使用"+
					p.Ir[p.pcb.instructID-1].runedtime+"秒\n");
			if(p.Ir[p.pcb.instructID-1].runedtime>=p.Ir[p.pcb.instructID-1].time) {
				screensQueue.remove(0);
				p.pcb.instructID++;
				if(p.pcb.instructID>p.Ir.length)
				{
					try {
						p.ProcessCancel();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//当前进程执行完毕，进程撤销原语撤销
					common.proresAppend("进程执行完，进程撤销！\n");
				}
				else {
					Thequeue.ready.add(p);
					common.proresAppend("进程" + p.pcb.ProID + "释放屏幕,进入就绪队列\n");
				}
				if(screensQueue.size()>0) {
					screens = screensQueue.get(0).pcb.ProID;
					common.proresAppend("进程" + p.pcb.ProID + "申请屏幕成功\n");
				}
				else
					screens = 0;
				return p;
			}
		}

		return null;
	}
	
	public Process VP() {  //互斥访问V(printers)

		if(printers != 0) {
			Process p = printersQueue.get(0);  //阻塞队列队头元素出队
			p.Ir[p.pcb.instructID-1].runedtime++;
			common.proresAppend("进程"+p.pcb.ProID+"使用打印机"+p.Ir[p.pcb.instructID-1].time+"秒,已使用"+
					p.Ir[p.pcb.instructID-1].runedtime+"秒\n");
			if(p.Ir[p.pcb.instructID-1].runedtime>=p.Ir[p.pcb.instructID-1].time) {
				printersQueue.remove(0);
				p.pcb.instructID++;
				if(p.pcb.instructID>p.Ir.length)
				{
					try {
						p.ProcessCancel();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//当前进程执行完毕，进程撤销原语撤销
					common.proresAppend("进程执行完，进程撤销！\n");
				}
				else {
					Thequeue.ready.add(p);
					common.proresAppend("进程" + p.pcb.ProID + "释放打印机,进入就绪队列\n");
				}
				if(printersQueue.size()>0) {
					printers = printersQueue.get(0).pcb.ProID;
					common.proresAppend("进程" + p.pcb.ProID + "申请打印机成功\n");
				}
				else
					printers = 0;
				return p;
			}
		}

		return null;
	}
	
	public Process VK() {  //互斥访问V(full)
		if(keyboards != 0) {
			Process p = keyboardsQueue.get(0);  //阻塞队列队头元素出队
			p.Ir[p.pcb.instructID-1].runedtime++;
			common.proresAppend("进程"+p.pcb.ProID+"使用键盘"+p.Ir[p.pcb.instructID-1].time+"秒,已使用"+
					p.Ir[p.pcb.instructID-1].runedtime+"秒\n");
			if(p.Ir[p.pcb.instructID-1].runedtime>=p.Ir[p.pcb.instructID-1].time) {
				keyboardsQueue.remove(0);
				p.pcb.instructID++;;

				if(p.pcb.instructID>p.Ir.length)
				{
					try {
						p.ProcessCancel();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//当前进程执行完毕，进程撤销原语撤销
					common.proresAppend("进程执行完，进程撤销！\n");
				}
				else
				{
					Thequeue.ready.add(p);
					common.proresAppend("进程" + p.pcb.ProID + "释放键盘,进入就绪队列\n");
				}
				if(keyboardsQueue.size()>0) {
					keyboards = keyboardsQueue.get(0).pcb.ProID;
					common.proresAppend("进程" + p.pcb.ProID + "申请键盘成功\n");
				}
				else
					keyboards = 0;
				return p;
			}
		}

		return null;
	}

	public Process VR() {  //互斥访问V(full)
		if(reads != 0) {
			Process p = readsQueue.get(0);  //阻塞队列队头元素出队
			p.Ir[p.pcb.instructID-1].runedtime++;
			common.proresAppend("进程"+p.pcb.ProID+"读磁盘"+p.Ir[p.pcb.instructID-1].time+"秒,已使用"+
					p.Ir[p.pcb.instructID-1].runedtime+"秒\n");
			if(p.Ir[p.pcb.instructID-1].runedtime>=p.Ir[p.pcb.instructID-1].time) {
				readsQueue.remove(0);
				p.pcb.instructID++;
				if(p.pcb.instructID>p.Ir.length)
				{
					try {
						p.ProcessCancel();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//当前进程执行完毕，进程撤销原语撤销
					common.proresAppend("进程执行完，进程撤销！\n");
				}
				else {
					Thequeue.ready.add(p);
					common.proresAppend("进程"+p.pcb.ProID+"停止读磁盘,进入就绪队列\n");
				}

				if(readsQueue.size()>0) {
					reads = readsQueue.get(0).pcb.ProID;
					common.proresAppend("进程" + p.pcb.ProID + "申请读磁盘成功\n");
				}
				else
					reads= 0;
				return p;
			}
		}

		return null;
	}
	public Process VW() {  //互斥访问V(full)
		if(writes != 0) {
			Process p = writesQueue.get(0);  //阻塞队列队头元素出队
			p.Ir[p.pcb.instructID-1].runedtime++;
			common.proresAppend("进程"+p.pcb.ProID+"写磁盘"+p.Ir[p.pcb.instructID-1].time+"秒,已使用"+
					p.Ir[p.pcb.instructID-1].runedtime+"秒\n");
			if(p.Ir[p.pcb.instructID-1].runedtime>=p.Ir[p.pcb.instructID-1].time) {
				writesQueue.remove(0);
				p.pcb.instructID++;
				if(p.pcb.instructID>p.Ir.length)
				{
					try {
						p.ProcessCancel();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}//当前进程执行完毕，进程撤销原语撤销
					common.proresAppend("进程执行完，进程撤销！\n");
				}
				else {
					Thequeue.ready.add(p);
					common.proresAppend("进程" + p.pcb.ProID + "停止写磁盘,进入就绪队列\n");
				}
				if(writesQueue.size()>0) {
					writes = writesQueue.get(0).pcb.ProID;
					common.proresAppend("进程" + p.pcb.ProID + "申请写磁盘成功\n");
				}
				else
					writes = 0;
				return p;
			}
		}

		return null;
	}

}
