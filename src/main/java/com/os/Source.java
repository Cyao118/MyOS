package com.os;

import java.util.*;


public class Source {
	public int full;
	public int empty;
	public int mutex;
	public List<Process> emptyQueue;  //阻塞队列
	public List<Process> fullQueue;  //阻塞队列
	public List<Process> mutexQueue;  //阻塞队列
	
	public Source() {
		full = 0;  //消费者可以消费0个
		empty = 2;  //生产者可以生产3个
		mutex = 1;  //互斥访问变量
		emptyQueue = new ArrayList<Process>();
		fullQueue = new ArrayList<Process>();
		mutexQueue = new ArrayList<Process>();
	}
	
	public boolean PE(Process p) {  //生产者P(empty)操作
		empty--;
		if(empty < 0) {  //无法得到资源
			emptyQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		return true;
	}
	
	public boolean PF(Process p) {  //生产者P(full)操作
		full--;
		if(full < 0) {  //无法得到资源
			fullQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		return true;
	}
	
	public boolean PM(Process p) {  //互斥访问P(mutex)
		mutex--;
		if(mutex < 0) {  //无法得到资源
			mutexQueue.add(p);  //进程进入当前资源的阻塞队列
			return false;
		}
		return true;
	}
	
	public Process VM() {  //互斥访问V(mutex)
		mutex++;
		if(mutex <= 0) {  
			Process p = mutexQueue.get(0);  //阻塞队列队头元素出队
			mutexQueue.remove(0);
			return p;
		}
		return null;
	}
	
	public Process VE() {  //互斥访问V(empty)
		empty++;
		if(empty <= 0) {  
			Process p = emptyQueue.get(0);  //阻塞队列队头元素出队
			emptyQueue.remove(0);
			return p;
		}
		return null;
	}
	
	public Process VF() {  //互斥访问V(full)
		full++;
		if(full <= 0) {  
			Process p = fullQueue.get(0);  //阻塞队列队头元素出队
			fullQueue.remove(0);
			return p;
		}
		return null;
	}
}
