package com.os;

import java.io.*;
import java.io.BufferedWriter;
import java.util.*;

public class management {
    public static int nowTime;//运行的总时间
	public Cpu cpu;
    public Memory mem;
    public Thequeue doqueue;  //存放队列的类
	public static Swap swap;  // 外存交换区
	public static int jobnum;
	public static LinkedList<JCB> jobTable;  // 后备作业队列
	public static Device[] deviceTable;  //进程死锁的设备资源
	public static Source source;
	public int flag;  //记录CPU当前指令执行的状态，进程切换需要进行现场恢复
	public int irtime;  //指令执行的时间
	public int pcblength;  //所有创建的进程
	//线程：系统时间变化
    private class time extends Thread{
		public void run() {
    		while(true) {
    			 management.nowTime++;
    	      try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		}
    	}
	//
    }
	//线程：CPU变化
    private class runcpu extends Thread{
		//public Cpu cpu=new Cpu();
    	File fp;
		public runcpu() {
			//this.cpu=cpu;
			fp = new File("Process.txt");
		}
		public void run() {
			
	    	
	    	BufferedWriter output = null;
			
			
			while(true) {
				try {
					output = new BufferedWriter(new FileWriter(new File("Process.txt"),true));
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
            	try {
					Thread.sleep(1000);
					Write_Frame.one.textArea[0].append("系统时间"+management.nowTime+"s\n");
					output.write("系统时间"+management.nowTime+"s\n");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	if(!cpu.Isuse) {  //现场恢复中CPU不忙，证明CPU空转，就绪队列中无进程可调入CPU
    				Write_Frame.one.textArea[0].append("\nCPU等待\n");
    				try {
						output.write("\nCPU等待\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    				flag = 0;
    				cpu.isEnd = true;  //需要进行进程调度
    				cpu.times = 3;  //时间片为3
    				//保存CPU信息到文件中
    				try {
						saveFile(-1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}   
    			}
    			else {  //正常执行
    				Write_Frame.one.textArea[0].append("\nCPU执行：" + nowTime + "\n");
    				if(cpu.times <=0) {	//剩余时间片不足以执行当前指令
    					irtime = 0;  //提前结束
    					//nowTime += irtime;
    					cpu.save();  //保护现场
    					doqueue.ready.add(cpu.pcb);
    					Write_Frame.one.textArea[0].append("当前运行进程时间片不足，进行进程切换\n");
    					try {
							output.write("当前运行进程时间片不足，进行进程切换\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    				else {
    					   					
    					try {
							cpu.MDR=cpu.mmu.l_to_p(cpu.pcb.Ir[cpu.PC-1].L_Address);
						} catch (IOException e7) {
							// TODO Auto-generated catch block
							e7.printStackTrace();
						}//MMU获取物理地址，PC指向第i条指令，在指令数组坐标要减一
    					if(cpu.MDR == -1) {		//未找到地址
    						try {
								if(pageInterrupt(cpu.pcb,(cpu.pcb.Ir[cpu.PC-1].L_Address >> 2)))  //缺页中断,从外存寻找进程的某一页调入内存
									//return false;
								cpu.pcb.pcb.page_register.length++;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}  //新的页表项进入内存，需要将页表的长度加一
    						//这里还要同步更新MMU中的页地址寄存器
    						cpu.mmu.page_regist.length++;
    						try {
								cpu.MDR=cpu.mmu.l_to_p(cpu.pcb.Ir[cpu.PC-1].L_Address);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}//MMU获取物理地址
    					}
    					cpu.PC++;  
    					//这里保护现场是要保存pc加后的值！
    					//读取指令
    					Instruct m = mem.readIR(cpu.MDR);
    					cpu.IR.setir(m.Instruct_ID, m.Instruct_State, m.L_Address);  //根据MDR中的物理地址读取内存获得指令
    						
    					try {
							flag=cpu.Execute();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  //cpu执行指令:0正常执行完，1P指令，2V指令，3阻塞，4关中断
    						
    					//输出执行信息
    					irtime = cpu.IR.time;
    					nowTime += irtime;
    					Write_Frame.one.textArea[0].append("执行的作业：" + cpu.pcb.JobID+"\n");
    					try {
							output.write("执行的作业：" + cpu.pcb.JobID+"\n");
						} catch (IOException e6) {
							// TODO Auto-generated catch block
							e6.printStackTrace();
						}
    					Write_Frame.one.textArea[0].append("执行的进程：" + cpu.pcb.pcb.ProID+"\n");
    					try {
							output.write("执行的进程：" + cpu.pcb.pcb.ProID+"\n");
						} catch (IOException e5) {
							// TODO Auto-generated catch block
							e5.printStackTrace();
						}
    					Write_Frame.one.textArea[0].append("剩余时间：" + cpu.times+"\n");
    					try {
							output.write("剩余时间：" + cpu.times+"\n");
						} catch (IOException e4) {
							// TODO Auto-generated catch block
							e4.printStackTrace();
						}
    					Write_Frame.one.textArea[0].append("完成的指令：" + "id: " + cpu.IR.Instruct_ID + "\tstate: " + cpu.IR.Instruct_State + "\ttimes: " + cpu.IR.time + "\tadd: " + cpu.IR.L_Address);
    					Write_Frame.one.textArea[0].append("\n当前进程占用的设备资源：\n");
    					try {
							output.write("\n当前进程占用的设备资源：\n");
						} catch (IOException e3) {
							// TODO Auto-generated catch block
							e3.printStackTrace();
						}
    					Write_Frame.one.textArea[0].append("Device1: " + cpu.pcb.nowd1 + "\tDevice2: " + cpu.pcb.nowd2 + "\n");
    					try {
							output.write("Device1: " + cpu.pcb.nowd1 + "\tDevice2: " + cpu.pcb.nowd2 + "\n");
						} catch (IOException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
    					//保存CPU信息到文件中
    					try {
							saveFile(flag);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}   
    					Write_Frame.one.textArea[9].setText("时间片\n"+cpu.times);
    					try {
							output.write("时间片\n"+cpu.times+"\n");
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    					//判断指令的执行情况，0正常执行完当前指令，1进程执行完，2进程阻塞需要进行上下文切换
    					if(flag == 0|cpu.PC > cpu.pcb.InstrucNum) {  //当前指令正常执行完
    						if(cpu.PC > cpu.pcb.InstrucNum) {  //当前进程执行完
    							cpu.save();
    							try {
									cpu.pcb.ProcessCancel();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}//当前进程执行完毕，进程撤销原语撤销
    							//进程撤销释放资源，会有阻塞进程进入就绪队列！
    							Write_Frame.one.textArea[0].append("进程执行完，进程撤销！\n");
    							try {
									output.write("进程执行完，进程撤销！\n");
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
    						}
    						else {
    							Write_Frame.one.textArea[0].append("当前进程继续执行！\n");//如果进程没有执行完，只是指令执行完，不需要保护现场！！
    							try {
									output.write("当前进程继续执行！\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
    						}	
    					}
    					else if(flag == 1) {   //当前进程需要进入普通阻塞队列
    						cpu.save();  //CPU现场保护
    						cpu.pcb.ProcessBlock();  //CPU中的进程进入阻塞队列
    						Write_Frame.one.textArea[0].append("进程" + cpu.pcb.pcb.ProID +"(作业" + cpu.pcb.JobID + ")进入普通阻塞队列！\n");
    					}
    					else{  //P指令当前进程已经进入阻塞队列
    						//这里不需要做什么
    						if(cpu.IR.Instruct_State == 6)
    							Write_Frame.one.textArea[0].append("进程进入Full阻塞队列\n");
    						if(cpu.IR.Instruct_State == 4)
    							Write_Frame.one.textArea[0].append("进程进入Mutex阻塞队列\n");
    						if(cpu.IR.Instruct_State == 8)
    							Write_Frame.one.textArea[0].append("进程进入Empty阻塞队列\n");
    						cpu.save();
    					}
    					cpu.times--;
    				}
    			}
    			//notifyAll();
    			//wait();
            	try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            }
		}
		//public synchronized boolean Execute() throws IOException, InterruptedException {  
			
		//}
	}
    //线程：三级调度
    private class scheduling extends Thread{
    	File fp;
        public void run() {
        	BufferedWriter output = null;
        	
    			while(true) {
    				try {
    					output = new BufferedWriter(new FileWriter(fp,true));
    				} catch (IOException e3) {
    					// TODO Auto-generated catch block
    					e3.printStackTrace();
    				}
    				try {
						Thread.sleep(1000);
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
    				//Write_Frame.one.textArea[0].append("\n中断处理例程：" + nowTime + "\n");
        			
        			if(doqueue.pcb_table.size()== 0 && nowTime != 0 && jobTable.size() == 0) {  //进行过程中所有进程执行完毕
        				Write_Frame.one.textArea[0].append("所有进程执行完毕！\n");
        				try {
							output.write("所有进程执行完毕！\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        				//return false;
        			}
        			
        			//死锁检测
        			int lock = -1;
        			while((lock = deadlockCheck()) != -1) {  //有死锁进程返回，每次释放一个进程，直到死锁接解除为止
        				try {
							deadlockRecover(lock);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        			}
        			
        			
        			//高级调度
        			JCB job = null;  
        			while(nowTime%5==0&&mem.isallocate() >= 4 && jobTable.size() > 0) {  //后备作业队列中有作业且内存空间足够
        				//PCB、页表、数据段、堆栈段
        				job = jobSchedule();  //从作业表中选出下一个进入内存的作业(同时删除作业队列中的作业)
        				if(job == null) {
        					Write_Frame.one.textArea[0].append("等待作业到达\n");break;//作业未到达
        				}
        				else {
        					Write_Frame.one.textArea[0].append("发生高级调度\n" + job.jobid + "号作业被创建为进程\n");
        					try {
								output.write("发生高级调度\n" + job.jobid + "号作业被创建为进程\n");
							} catch (IOException e2) {
								// TODO Auto-generated catch block
								e2.printStackTrace();
							}
        					Process p = new Process();  //申请空白PCB，真正的PCB空间
        					try {
								p.ProcessCreate(job, ++pcblength);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}  //进程创建
        					try {
        						
								saveProcess(p);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        					doqueue.ready.add(p);  //进程进入就绪队列
        					doqueue.pcb_table.add(p);//进程放入系统pcb表
        					job = null;  //作业已经创建完毕，释放
        					Write_Frame.one.textArea[0].append("进程" + p.pcb.ProID + "(作业" + p.JobID + ")创建\n");
        				}
        					
        			}
        			  //若无法创建进程，输出原因
        				if(jobTable.size() <= 0) {
        					Write_Frame.one.textArea[0].append("后备作业队列为空！\n");
        					try {
								output.write("后备作业队列为空！\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        				}
        				if(mem.isallocate() < 4) {
        					Write_Frame.one.textArea[0].append("内存空间不足！\n");
        					try {
								output.write("内存空间不足！\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        				}
        			
        				
        			//进程唤醒（普通的I/O阻塞队列从阻塞队列中出来，未到时间的减）
        			Process t = null;
        			if(nowTime%5==0&&doqueue.block.size()!=0) {//每5s检查一次阻塞队列，若存在则唤醒
        				t = doqueue.block.get(0);         //获取最早进入阻塞队列的指令
        				doqueue.block.remove(0);  //从阻塞队列出来 
        			    doqueue.ready.add(t);  //进入就绪队列
        			}
        			
        			
        			
        			Write_Frame.one.textArea[0].append("当前Device的资源数量：\n");
        			Write_Frame.one.textArea[0].append("Device1: " + deviceTable[0].value + "\tDevice2: " + deviceTable[1].value + "\n");
        			
        			try {
						showQueue();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}  //显示各个队列的情况
        			//中级调度
        			if(mem.isallocate() <4&&(Thequeue.block.size()>0|Thequeue.ready.size()>0)) {
        				Process l=null;
        				if(Thequeue.block.size()>0) {
        					l=Thequeue.block.getFirst();
        					Thequeue.block.remove(l);
        		
        				}
        				else {
        					l=Thequeue.ready.getFirst();
        					Thequeue.ready.remove();
        				}
        				    Thequeue.hang.add(l);
        				    doqueue.pcb_table.remove(l); 
        					
        					//释放内存空间
        					try {
								Memory.realeaseSpace(l);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        					deviceTable[0].addValue(l.pcb.ProID,l.nowd1,1);
        				    deviceTable[1].addValue(l.pcb.ProID,l.nowd1,2);
        				
        			}
        			if(mem.isallocate() >4&&Thequeue.hang.size()>0) {
        				Process l=null;
        				l=Thequeue.hang.getFirst();
    					Thequeue.hang.remove(l);
    					doqueue.pcb_table.add(l); 
    					Thequeue.hang.remove(l);
        			}
        			//低级调度
        			//这里应该是如果CPU需要进行进程切换的时候再进行进行调度，需要加一个标志位！
        			if(cpu.isEnd) {  //CPU需要进行进程调度的时候再进行
        				if(doqueue.ready.size() > 0) {  //就绪队列有可以进入的进程
        					Process waitpcb =null;
        					waitpcb=proSchedule();  //进程调度
        					if(waitpcb != null) {
        						cpu.recover(waitpcb);  //现场恢复：指明CPU在忙
        						flag = 0;
        			    	Write_Frame.one.textArea[0].append("进程调度选择" + waitpcb.pcb.ProID  + "号进程" + "\n");
        			    	try {
								output.write("进程调度选择" + waitpcb.pcb.ProID  + "号进程" + "\n");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
        					}
        				}
        				else {  //此时阻塞队列为空
        					Write_Frame.one.textArea[0].append("就绪队列为空\n");
        				}
        			}
        			try {
						output.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			//notifyAll();
        			//wait();
        			//return true;
        		}
    			}
    	public scheduling() {
    		fp = new File("Process.txt");
    	}
    	
    }
    //初始化
    public management() throws IOException { 
		cpu = new Cpu();
		mem = new Memory();
		swap = new Swap(); 
		source = new Source();
		deviceTable = new Device[3];  //假设只有三个
		for(int i = 0; i < 3; i++) {
			deviceTable[i] = new Device();
		}
		jobTable = new LinkedList<JCB>();
		doqueue=new Thequeue();
		nowTime = 0;  //初始时钟为-1
		jobnum=4;
		flag = 0;
		irtime = 0;
		pcblength = 0;
		File file = new File("Process.txt");
		if(!file.exists())
			file.createNewFile();  //创建文件
		else {  //清空文件
			FileWriter fw = new FileWriter(file);
			fw.write("");
			fw.close();
		}
		File f = new File("allprocess.txt");
		if(!file.exists())
			file.createNewFile();  //创建文件
		else {  //清空文件
			FileWriter fw = new FileWriter(f);
			fw.write("ID\tPri\tCreat\td1\td2\tirnum\tid\tstate\ttimes\tadd\r\n");
			fw.close();
		}
		Write_Frame.one.textArea[0].append("正在读取作业：\n");
		runcpu two=new runcpu();
		time one=new time();
		scheduling three=new scheduling();
		readJob();  //从文件中读取全部作业，放入后备作业队列
		three.start();
		two.start();
		one.start();
	}

	public void saveProcess(Process p) throws IOException {
		File file = new File("allprocess.txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(String.valueOf(p.pcb.ProID) + "\t" +
				String.valueOf(p.pcb.Priority) + "\t" + 
				String.valueOf(p.pcb.createtime) + "\t" + 
				String.valueOf(p.d1) + "\t" + 
				String.valueOf(p.d2) + "\t" + 
				String.valueOf(p.InstrucNum) + "\t");
		
	bw.write(String.valueOf(p.Ir[0].Instruct_ID) + "\t" +
				String.valueOf(p.Ir[0].Instruct_State) + "\t" + 
				String.valueOf(p.Ir[0].time) + "\t" +
				String.valueOf(p.Ir[0].L_Address) + "\r\n");
		for(int i = 1; i < p.InstrucNum; i++) {
			bw.write("\t\t\t\t\t\t" + String.valueOf(p.Ir[i].Instruct_ID) + "\t" +
					String.valueOf(p.Ir[i].Instruct_State) + "\t" + 
					String.valueOf(p.Ir[i].time) + "\t" +
					String.valueOf(p.Ir[i].L_Address) + "\r\n");
		}
		bw.close();
	}
	
	public void saveFile(int flag) throws IOException {
		File file = new File("Process.txt");
		//FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file,true));
		if(flag == -1) {  //CPU空等
			bw.write("\r\nCPU空闲等待\r\n\r\n");
		}
		else {
			bw.write("\r\nCPU正在执行指令的相关信息\r\n");
			bw.write("\r\nProID\t    JobID\t  InstrucNum\t   InstructID\t InstructState\t  InstructTimes\r\n\r\n");		
			bw.write(String.valueOf(cpu.pcb.pcb.ProID) + "\t\t"+ 
					String.valueOf(cpu.pcb.JobID) + "\t\t" + 
					String.valueOf(cpu.pcb.InstrucNum) + "\t\t" + 
					String.valueOf(cpu.IR.Instruct_ID) + "\t\t" + 
					String.valueOf(cpu.IR.Instruct_State) + "\t\t" + 
					String.valueOf(cpu.IR.time) + "\r\n\r\n");
			if(flag == 0) {  //当前指令正常执行完
				if(cpu.PC > cpu.pcb.InstrucNum) {  //当前进程执行完
					bw.write("进程执行完\r\n");
				}
				else{
					bw.write("指令执行完\r\n");//如果进程没有执行完，只是指令执行完，不需要保护现场！
				}
			}
			else if(flag == 1) {   //当前进程需要进入普通阻塞队列
				bw.write("进程进入阻塞队列\r\n");				
			}
			else{  //P指令当前进程已经进入阻塞队列
				if(cpu.IR.Instruct_State == 6){
					bw.write("进程等待FULL,进入阻塞\r\n");
				}
				if(cpu.IR.Instruct_State == 4){
					bw.write("进程等待mutex，进入阻塞\r\n");
				}
				if(cpu.IR.Instruct_State == 8){
					bw.write("进程等待EMPTY，进入阻塞\r\n");
				}
			}
		}
		bw.close();
	}

	public boolean pageInterrupt(Process p, int page) throws IOException {  //缺页中断
		
		SwapBlock sb = Swap.search(p.pcb.ProID,page);  //根据页号+进程号搜索外页表（外存上），找到相应进程的装入信息；(这里要分配内存)
		//找到之后同时也要删除进程所在的外交换区
		boolean flag = false;
		if(sb != null) {  //外存上找到该页	
			int bp = -1;
			if(mem.isallocate() > 0) {  //内存有空闲位置，分配一个（修改进程页表和系统页表）；
				bp = mem.allocateSpace();  //返回空闲的物理块号
				flag = true;
			}
			else {  //没有空闲位置
				bp = pageReplace();  //页面替换算法，选中一页换出去，返回选中的物理块号
				Swap.setBlock(Memory.block[bp]);  //将替换出去的物理块写到交换区
				mem.deletePage(Memory.block[bp].proid,Memory.block[bp].status - 1);//被替换出去的写到外存交换区，同时还要删除替换出的那一块所属进程的页框表
				Write_Frame.one.textArea[0].append("页面替换选择将" + bp + "号物理块的页面换出！\n");
				flag = false;
			}
			if(bp != -1) {  //内存中找到物理块
				Memory.modifyBlock(p, page, bp, flag);  //更新物理块信息，将该页放入内存
				//p.pcb.page_register.pageAddress=bp;//
				Memory.updatePage(p.pcb.page_register.pageAddress,p.pcb.ProID,page,bp);  //写入该进程的页表（内存中）
				Write_Frame.one.textArea[0].append("缺页中断将当前进程" + page + "号页面装入" + bp + "号物理块！\n");
			}
			else {
				Write_Frame.one.textArea[0].append("缺页中断，内存分配失败！\n");
				return false;  //程序终止！
			}
		}
		else {  //外存上没有找到该页
			Write_Frame.one.textArea[0].append("外存上不存在" + p.pcb.ProID + "号进程(作业" + p.JobID + ")的" + page + "号页！\n错误！\n");
			return false;  //程序错误，退出！！！！！！！！
		}
		return true;
	}
	
	public int pageReplace() {  //页面替换算法，选中一页换出去，返回选中的物理块号
		
		int temp = -1;
		
		temp = mem.LRU();  //从存放有其它进程页面的物理块中选中一块
		
		return temp;
	}
	
	public JCB jobSchedule() {  //高级调度算法，返回一个作业,优先级数越小,优先级越高//先来先服务加优先级
		JCB b = null; 
		int min = 0;  
		for(int i = 0; i < jobTable.size(); i++) {
			if(jobTable.get(i).arriveTime <= nowTime) {  
				//当前优先级数字小且该作业已经创建
					min = i;break;
			}
		}
		if(min == 0) {  //选择的作业未改变是默认的第一个，但是要检测第一个的作业到达时间
			if(jobTable.get(min).arriveTime > nowTime)  //作业未到达
				return null;
		}
		b = jobTable.get(min);
		jobTable.remove(min);  //获得并删除
		return b;
	}
	
	//低级调度，优先级调度，修改Control里的等待进程
	public Process proSchedule() {  
		Process p = null;
		int min = 0;
		/*for(int i = 1; i < Thequeue.ready.size(); i++) {//优先级
			if(Thequeue.ready.get(i).pcb.Priority < Thequeue.ready.get(min).pcb.Priority)
				min = i;
		}*/
		p = this.doqueue.ready.get(min);
		this.doqueue.ready.remove(min);
		return p;
	}
	
	//从内存中读取作业到jobTable中  
	public void readJob() throws IOException {  
		
		FileReader fp = new FileReader("19317124-jobs-input.txt");
        BufferedReader in = new BufferedReader(fp);
		try {
			String line;
			line = in.readLine();  //先把第一行抛出来（标题行）
			int i=1;
				while((line = in.readLine()) != null&&i<=jobnum) {  //读入一行（每个进程的第一行）
					writeJob(line,in,i);  //该函数中会继续往后读取，直到进程的指令读取完毕，将一个进程写入job表中
				    i++;
				}
			
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		in.close();
		
	}
	
	//将一个作业写入jobTable表中
	public void writeJob(String line,BufferedReader br,int link) throws IOException {  //当前进程写入jobTable表中
	
		JCB j = new JCB();  //申请空白作业
		String[] p = line.split("	");
		
		j.jobid = Integer.parseInt(p[0]);
		j.priority = Integer.parseInt(p[1]);
		j.arriveTime = Integer.parseInt(p[2]);
		
		j.d1 = Integer.parseInt(p[3]);
		j.d2 = Integer.parseInt(p[4]);
		//j.d3 = Integer.parseInt(p[5]);
		j.instructNum = Integer.parseInt(p[5]);
		j.psw =1;
		j.IR = new Instruct[j.instructNum];
		for(int i = 0 ;i < j.instructNum; i++) {
			j.IR[i] = new Instruct();
		}
		FileReader fp = new FileReader(link+".txt");
		
        BufferedReader in = new BufferedReader(fp);
        String line2;
        line2 = in.readLine();
		int m = 0;
		//j.IR[m].Instruct_ID = Integer.parseInt(p[8]);
		//j.IR[m].Instruct_State = Integer.parseInt(p[9]);
		//j.IR[m].time = Integer.parseInt(p[10]);
		//m++;
		
		while(m < j.instructNum && (line2 = in.readLine()) != null) {
			String[] t = line2.split("	");
			
			j.IR[m].Instruct_ID = Integer.parseInt(t[0]);
			
			j.IR[m].Instruct_State = Integer.parseInt(t[2]);
		
			j.IR[m].time = 0;
			m++;
		}
		
		jobTable.add(j);
	}
    public void showmem() {
    	int i=0;
    	for(;i<64;i++) {
    		Write_Frame.one.textArea[1].append(mem.block[i].status+"  ");
    		if((i+1)%8==0&&i!=0)
    			Write_Frame.one.textArea[1].append("\n");	
    	}
    }
	public void showQueue() throws IOException {  //队列中进程显示//移动到thequeue中
		
		
		File fp = new File("Process.txt");
		BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
		Process t =new Process();
		Write_Frame.one.textArea[1].setText(" ");
		Write_Frame.one.textArea[1].append("系统时间:" + nowTime + "\n");
		showmem();
		output.write("\n系统时间:" + nowTime + "\n");
		 Write_Frame.one.textArea[8].setText(" ");
		if(cpu.Isuse) {
			   Write_Frame.one.textArea[8].append("运行：\n"+cpu.pcb.pcb.ProID);
			   output.write("运行：\n"+cpu.pcb.pcb.ProID);
		  }
		if(jobTable.size() >=0) {  //作业后备队列
			Write_Frame.one.textArea[2].setText(" ");
			Write_Frame.one.textArea[2].append("后备作业队列：\nJobID\tPriority\tInTimes\n");
			output.write("后备作业队列：\nJobID\tPriority\tInTimes\n");
			JCB j = null;
			for(int i = 0; i < jobTable.size(); i++) {
				j = jobTable.get(i);
				Write_Frame.one.textArea[2].append(j.jobid + "\t" + j.priority + "\t" + j.arriveTime+"\n");
				output.write(j.jobid + "\t" + j.priority + "\t" + j.arriveTime+"\n");
			}
		}
		if(doqueue.ready.size() >=0){  //就绪队列
			Write_Frame.one.textArea[3].setText(" ");
			Write_Frame.one.textArea[3].append("就绪队列：\nProID\tjobID\n");
			for(int i = 0; i < doqueue.ready.size(); i++) {
				t= doqueue.ready.get(i);
				Write_Frame.one.textArea[3].append(t.pcb.ProID + "\t" +"\n");
			}
		}
		
		if(doqueue.block.size() >=0) {  //普通阻塞队列
			Write_Frame.one.textArea[4].setText(" ");
			Write_Frame.one.textArea[4].append("普通阻塞队列：\nProID\tjobID\n");
			for(int i = 0; i < doqueue.block.size(); i++) {
				t= doqueue.block.get(i);
				Write_Frame.one.textArea[4].append(t.pcb.ProID + "\t" +"\n");
			}
		}
		
		for(int i = 0; i < 2; i++) {  //3个资源阻塞队列
			Write_Frame.one.textArea[i+11].setText(" ");
			Write_Frame.one.textArea[i+11].append("资源" + (i+1) + "：ProID\tjobID\n");
			
			if(deviceTable[i].deviceQueue.size() >0) {
				System.out.println("!!!!!");
				for(int k = 0; k < deviceTable[i].deviceQueue.size(); k++) {
					t = deviceTable[i].deviceQueue.get(k);
					Write_Frame.one.textArea[i+11].append(t.pcb.ProID + "\t" + t.JobID+"\n");
				}
			}
		}
		
		if(source.mutexQueue.size() >= 0) {
			Write_Frame.one.textArea[5].setText(" ");
			Write_Frame.one.textArea[5].append("mutex阻塞队列：\nProID\tjobID\n");
			for(int k = 0; k < source.mutexQueue.size(); k++) {
				t = source.mutexQueue.get(k);
				Write_Frame.one.textArea[5].append(t.pcb.ProID + "\t" +"\n");
			}
		}
		
		if(source.fullQueue.size() >=0) {
			Write_Frame.one.textArea[6].setText(" ");
			Write_Frame.one.textArea[6].append("full阻塞队列：\nProID\tjobID\n");
			for(int k = 0; k < source.fullQueue.size(); k++) {
				t = source.fullQueue.get(k);
				Write_Frame.one.textArea[6].append(t.pcb.ProID + "\t" +"\n");
			}
		}
		
		if(source.emptyQueue.size() >=0) {
			Write_Frame.one.textArea[7].setText(" ");
			Write_Frame.one.textArea[7].append("empty阻塞队列：\nProID\tjobID\n");
			for(int k = 0; k < source.emptyQueue.size(); k++) {
				t = source.emptyQueue.get(k);
				Write_Frame.one.textArea[7].append(t.pcb.ProID + "\t" +"\n");
			}
		}
		if(Thequeue.hang.size()>=0) {
			Write_Frame.one.textArea[10].setText(" ");
			Write_Frame.one.textArea[10].append("挂起队列：\nProID\tjobID\n");
			for(int k = 0; k < Thequeue.hang.size(); k++) {
				t = Thequeue.hang.get(k);
				Write_Frame.one.textArea[10].append(t.pcb.ProID + "\t" +"\n");
			}
		}
	}
    
	public int deadlockCheck() {  //死锁检测：针对于进程中的device设备
		
		int num = doqueue.pcb_table.size();  //当前系统内的进程数
		Process m = new Process();
		
		int[][] allocation = new int[num][2];  //进程已经得到的资源
		int[][] request = new int[num][2];  //进程还需每类资源的数目
		int[] work = new int[2];  //当前系统内每类资源还可以分配的资源数目
		boolean[] finish = new boolean[num];
		int j = 0,k = 0;
		for(; k < num; k++) {
			m = doqueue.pcb_table.get(k);
			if(m.nowd1<0)
			allocation[k][0] = 0;
			else
			allocation[k][0] = m.nowd1;	
			if(m.nowd2<0)
				allocation[k][0] = 0;
			else
				allocation[k][0] = m.nowd2;	
			for(j = 0; j < 2; j++) {   //request数组是当前进程要求分配资源确不能够得到，因为每次执行指令只会得到1个资源
				//所以该数组的值不是1就是0
				if(deviceTable[j].searchQueue(m.pcb.ProID))  //当前进程在阻塞队列里面，证明当前进程需要申请资源
					request[k][j] = 1;
				else
					request[k][j] = 0;
			}
		}//在阻塞队列里面的进程不一定会死锁，发生死锁的一定在阻塞队列里面
		for(j = 0; j < 2; j++) {  
			if(deviceTable[j].value < 0)
				work[j] = 0;  //最小为0
			else
				work[j] = deviceTable[j].value;
		}
		for(j = 0; j <doqueue.pcb_table.size(); j++) {
			finish[j] = true;
		}
			
		for(k = 0; k < doqueue.pcb_table.size(); k++) {
			m = doqueue.pcb_table.get(k);
			if((deviceTable[0].searchQueue(m.pcb.ProID)&&m.nowd2>0&&deviceTable[1].deviceQueue.size()>0)|(deviceTable[1].searchQueue(m.pcb.ProID)&&m.nowd1>0&&deviceTable[0].deviceQueue.size()>0)) {
				//work[0] += allocation[k][0];
				//work[1] += allocation[k][1];
			
				finish[k] = false;
			}
			if((request[k][0] <= work[0]) && (request[k][1] <= work[1])) {
				work[0] += allocation[k][0];
				work[1] += allocation[k][1];
				
			}
			else {
				finish[k] = false;
			}
		}
		for(k = 0; k < doqueue.pcb_table.size(); k++) {
			if(finish[k] == false) 
				return k;
		}
		return -1;
	}
	
	public void deadlockRecover(int lock) throws IOException {  //死锁恢复,lock号进程放入作业队列
		
		Write_Frame.one.textArea[0].append("死锁恢复：");
		
		//从PCB表中删除该进程
		Process l = new Process();
		l=doqueue.pcb_table.get(lock);
		doqueue.pcb_table.remove(l); 
		
		//如果要撤销的进程是当前CPU正在执行的进程
		if(cpu.pcb.pcb.ProID == l.pcb.ProID)
			cpu.save();  //CPU现场恢复
		
		//释放内存空间
		Memory.realeaseSpace(l);	
		
		//普通阻塞队列
		for(int i = 0; i < doqueue.block.size(); i++) {
			if(doqueue.block.get(i).pcb.ProID==l.pcb.ProID) {
				doqueue.block.remove(i);
				i--;
			}
		}
		
		//就绪队列
		for(int i = 0; i < doqueue.ready.size(); i++) {
			if(doqueue.ready.get(i).pcb.ProID == l.pcb.ProID) {
				doqueue.ready.remove(i);
				i--;
			}
		}
		
		//清空进程在外存交换区的内容
		swap.deletePro(l.pcb.ProID);
		
		//撤销占用的资源,找到当前进程所在的资源阻塞队列，从阻塞队列中出队
		Write_Frame.one.textArea[0].append("进程" + l.pcb.ProID + "(作业" + l.JobID + ")释放" + l.nowd1 + "个1号资源！" + l.nowd2 + "个2号资源！\n");
		this.deviceTable[0].addValue(l.pcb.ProID,l.nowd1,1);
		this.deviceTable[1].addValue(l.pcb.ProID,l.nowd1,2);
		//this.deviceTable[2].addValue(l.pcb.ProID,l.d3num,3);
		//pcblength--;
		//重新进入后备作业队列
		JCB j = new JCB();
		j.setJob(l);  //进程重新成为作业?????????
		jobTable.add(j);
	}
}
