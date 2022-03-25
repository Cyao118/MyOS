package com.os;

import java.util.*;


public class Device {
	public int value;  //该设备是否被分配
	public LinkedList<Process> deviceQueue;  //对应的阻塞队列
	
	public Device() {
		value = 3;
		deviceQueue = new LinkedList<Process>();
	}
	
	public void addValue(int id,int n,int m) {  //进程撤销时，释放占有的资源
		Process p = null;
		int i = 0;
		for(; i < deviceQueue.size(); i++) {  //看当前资源队列里面有没有要撤销的进程，有的话，出队，
			p = deviceQueue.get(i);
			if(p.pcb.ProID == id) {  //撤销的进程在阻塞队列里面
				value++;
				deviceQueue.remove(i);
				break;
			}
		}
		for(i = 0; i < n && (deviceQueue.size() > 0); i++) {  
			//唤醒一个进程，唤醒的进程就会获得资源，需要将唤醒进程的资源数++
			p = deviceQueue.get(0);  //撤销一个资源，唤醒阻塞队列中的一个进程
			if(m == 1)
				p.nowd1++;
			if(m == 2)
				p.nowd2++;
			//if(m == 3)
				//p.nowd3++;
			Thequeue.ready.add(p);  //从资源阻塞队列出来后进入系统的就绪队列
			Write_Frame.one.textArea[0].append("进程" + p.pcb.ProID + "(作业" + p.JobID + ")从" + m + "号资源阻塞队列唤醒！\n");
			deviceQueue.remove(0);
		}
		value += n;
	}
	
	public boolean searchQueue(int jobID) {
		for(int i = 0; i < deviceQueue.size(); i++) {
			if(deviceQueue.get(i).JobID == jobID) {  //要撤销的进程在阻塞队列里面
				return true;
			}
		}
		return false;
	}
	
	public boolean P(Process p) {  //对资源的操作
		value--;
		if(value < 0) {  //无法得到资源
			deviceQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		return true;  //可以分配得到资源
	}
	
	public Process V() {  //对资源的操作
		value++;
		if(value <= 0) {  //释放一个资源，就唤醒一个进程
			Process p = deviceQueue.get(0);  //阻塞队列队头元素出队
			deviceQueue.remove(0);
			return p;
		}
		return null;
	}
}
