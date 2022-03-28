package com.os;

import org.omg.CORBA.IRObject;

import java.io.*;
import java.io.BufferedWriter;
import java.util.*;

public class management {
    public static int nowTime;//运行的总时间
	public static Cpu cpu;
    public static Memory mem;
    
	public static Swap swap;  // 外存交换区
	public static int jobnum;
	public static LinkedList<JCB> jobTable;  // 后备作业队列
	public static Device[] deviceTable;  //进程死锁的设备资源
	public static Source source;
	public int flag;  //记录CPU当前指令执行的状态，进程切换需要进行现场恢复
	public int irtime;  //指令执行的时间
	public static int pcblength;  //所有创建的进程
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
					common.proresAppend("系统时间"+management.nowTime+"s\n");
					output.write("系统时间"+management.nowTime+"s\n");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	if(!cpu.Isuse) {  //现场恢复中CPU不忙，证明CPU空转，就绪队列中无进程可调入CPU
    				common.proresAppend("\nCPU等待\n");
    				try {
						output.write("\nCPU等待\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    				flag = 0;
    				cpu.isEnd = true;  //需要进行进程调度
    				cpu.times = common.timeslice;  //时间片为3
    				//保存CPU信息到文件中
    				try {
						saveFile(-1);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}   
    			}
    			else {  //正常执行
    				common.proresAppend("\nCPU执行：" + nowTime + "\n");
					if(cpu.PC>cpu.pcb.Ir.length) {  //当前进程执行完
						cpu.save();
						try {
							cpu.pcb.ProcessCancel();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}//当前进程执行完毕，进程撤销原语撤销
						//进程撤销释放资源，会有阻塞进程进入就绪队列！
						common.proresAppend("进程执行完，进程撤销！\n");
						try {
							output.write("进程执行完，进程撤销！\n");

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}

    				if(cpu.times==0||cpu.times <(cpu.pcb.Ir[cpu.PC-1].time-cpu.pcb.Ir[cpu.PC-1].runedtime)) {	//剩余时间片不足以执行当前指令
    					irtime = 0;  //提前结束
    					//nowTime += irtime;
    					cpu.save();  //保护现场
    					Thequeue.ready.add(cpu.pcb);
    					common.proresAppend("当前运行进程时间片不足，进行进程切换\n");
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
								pageInterrupt(cpu.pcb,(cpu.pcb.Ir[cpu.PC-1].L_Address >> 2));  //缺页中断,从外存寻找进程的某一页调入内存
									//return false;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}  //新的页表项进入内存，需要将页表的长度加一
    						//这里还要同步更新MMU中的页地址寄存器
    						try {
								cpu.MDR=cpu.mmu.l_to_p(cpu.pcb.Ir[cpu.PC-1].L_Address);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}//MMU获取物理地址
    					}

    					//这里保护现场是要保存pc加后的值！
    					//读取指令
						if(cpu.MDR!=-1) mem.readIR(cpu.MDR);
    					Instruct m= cpu.pcb.Ir[cpu.PC-1];
//    					System.out.println(""+nowTime+"\t"+cpu.pcb.pcb.ProID+"\t"+cpu.MDR+"\t"+cpu.pcb.pcb.instructID+'\t'+cpu.times);
    					cpu.IR.setir(m.Instruct_ID, m.Instruct_State, m.L_Address,m.time,m.runedtime);  //根据MDR中的物理地址读取内存获得指令
    					common.proresAppend("执行的作业：" + cpu.pcb.JobID+"\n");
    					try {
							output.write("执行的作业：" + cpu.pcb.JobID+"\n");
						} catch (IOException e6) {
							// TODO Auto-generated catch block
							e6.printStackTrace();
						}
    					common.proresAppend("执行的进程：" + cpu.pcb.pcb.ProID+"\n");
    					try {
							output.write("执行的进程：" + cpu.pcb.pcb.ProID+"\n");
						} catch (IOException e5) {
							// TODO Auto-generated catch block
							e5.printStackTrace();
						}

						try {
							flag=cpu.Execute();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}  //cpu执行指令:0正常执行完，1P指令，2V指令，3阻塞，4关中断


						//输出执行信息
						irtime = cpu.IR.time;
						nowTime += irtime;

    					common.proresAppend("剩余时间：" + cpu.times+"\n");
    					try {
							output.write("剩余时间：" + cpu.times+"\n");
						} catch (IOException e4) {
							// TODO Auto-generated catch block
							e4.printStackTrace();
						}
    					common.proresAppend("完成的指令：" + "\n指令ID（InstructId）: " + cpu.IR.Instruct_ID + "\n指令类型（InstructState）: " + cpu.IR.Instruct_State + "\n指令内容（InstructDescription）: " + InstructTypes.getInstructByState(cpu.IR.Instruct_State).getDescription() + "\n运行时间（InRunTimes）: " + InstructTypes.getInstructByState(cpu.IR.Instruct_State).getRuntime() +"\n已运行时间（InRunedTimes）: " + cpu.IR.runedtime+ "\n逻辑地址（L_Address）: " + cpu.IR.L_Address+"\n物理地址（_Address）: "+cpu.MDR+"\n");
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
    					if(flag == 0||cpu.PC > cpu.pcb.InstrucNum) {  //当前指令正常执行完
    						if(cpu.PC > cpu.pcb.InstrucNum) {  //当前进程执行完
    							cpu.save();
    							try {
									cpu.pcb.ProcessCancel();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}//当前进程执行完毕，进程撤销原语撤销
    							//进程撤销释放资源，会有阻塞进程进入就绪队列！
    							common.proresAppend("进程执行完，进程撤销！\n");
    							try {
									output.write("进程执行完，进程撤销！\n");
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
    						}
    						else {
    							common.proresAppend("当前进程继续执行！\n");//如果进程没有执行完，只是指令执行完，不需要保护现场！！
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
    						common.proresAppend("进程" + cpu.pcb.pcb.ProID +"(作业" + cpu.pcb.JobID + ")进入普通阻塞队列！\n");
    					}
    					else{  //P指令当前进程已经进入阻塞队列
    						//这里不需要做什么
							if(cpu.IR.Instruct_State == 2)
								common.proresAppend("进程进入键盘输入阻塞队列\n");
    						if(cpu.IR.Instruct_State == 3)
    							common.proresAppend("进程进入屏幕显示阻塞队列\n");
    						if(cpu.IR.Instruct_State == 4)
    							common.proresAppend("进程进入读磁盘阻塞队列\n");
    						if(cpu.IR.Instruct_State == 5)
    							common.proresAppend("进程进入写磁盘阻塞队列\n");
							if(cpu.IR.Instruct_State == 6)
								common.proresAppend("进程进入打印阻塞队列\n");
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

			while (true) {
				try {
					output = new BufferedWriter(new FileWriter(fp, true));
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
					//common.proresAppend("\n中断处理例程：" + nowTime + "\n");

				if (Thequeue.pcb_table.size() == 0 && nowTime != 0 && jobTable.size() == 0) {  //进行过程中所有进程执行完毕
						common.proresAppend("所有进程执行完毕！\n");
						try {
							output.write("所有进程执行完毕！\n");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//return false;
				}



					//低级调度
					//这里应该是如果CPU需要进行进程切换的时候再进行进行调度，需要加一个标志位！
				if (cpu.isEnd) {  //CPU需要进行进程调度的时候再进行
						if (Thequeue.ready.size() > 0) {  //就绪队列有可以进入的进程
							Process waitpcb = null;
							waitpcb = proSchedule();  //进程调度
							if (waitpcb != null) {
								cpu.recover(waitpcb);  //现场恢复：指明CPU在忙
								flag = 0;
								common.proresAppend("进程调度选择" + waitpcb.pcb.ProID + "号进程" + "\n");
								try {
									output.write("进程调度选择" + waitpcb.pcb.ProID + "号进程" + "\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else {  //此时阻塞队列为空
							common.proresAppend("就绪队列为空\n");
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
		common.proresAppend("正在读取作业：\n");
		runcpu two=new runcpu();
		time one=new time();
		scheduling three=new scheduling();
		JobSheduling jobSheduling = new JobSheduling();
		PageSheduling pageSheduling = new PageSheduling();
		WakeSheduling wakeSheduling = new WakeSheduling();
		readJob();  //从文件中读取全部作业，放入后备作业队列
		three.start();
		two.start();
		one.start();
		wakeSheduling.start();
		jobSheduling.start();
		pageSheduling.start();
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
				common.proresAppend("页面替换选择将" + bp + "号物理块的页面换出！\n");
				flag = false;
			}
			if(bp != -1) {  //内存中找到物理块
				Memory.modifyBlock(p, page, bp, flag);  //更新物理块信息，将该页放入内存
				//p.pcb.page_register.pageAddress=bp;//
				Memory.updatePage(p.pcb.page_register.pageAddress,p.pcb.ProID,page,bp);  //写入该进程的页表（内存中）
				common.proresAppend("内存第"+bp+"块被进程"+p.pcb.ProID+"第"+page+"页占用\n");
			}
			else {
				common.proresAppend("缺页中断，内存分配失败！\n");
				return false;  //程序终止！
			}
		}
		else {  //外存上没有找到该页
			common.proresAppend("外存上不存在" + p.pcb.ProID + "号进程(作业" + p.JobID + ")的" + page + "号页！\n错误！\n");
			return false;  //程序错误，退出！！！！！！！！
		}
		return true;
	}
	
	public int pageReplace() {  //页面替换算法，选中一页换出去，返回选中的物理块号
		
		int temp = -1;
		
		temp = mem.LRU();  //从存放有其它进程页面的物理块中选中一块
		
		return temp;
	}
	

	
	//低级调度，优先级调度，修改Control里的等待进程
	public Process proSchedule() {  
		Process p = null;
		int min = 0;
		/*for(int i = 1; i < Thequeue.ready.size(); i++) {//优先级
			if(Thequeue.ready.get(i).pcb.Priority < Thequeue.ready.get(min).pcb.Priority)
				min = i;
		}*/
		p = Thequeue.ready.get(min);
		Thequeue.ready.remove(min);
		return p;
	}
	
	//从内存中读取作业到jobTable中  
	public void readJob() throws IOException {  
		
		FileReader fp = new FileReader(common.user+"-jobs-input.txt");
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
		j.instructNum = Integer.parseInt(p[3]);
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

			j.IR[m].L_Address = Integer.parseInt(t[4]);

			j.IR[m].time = Integer.parseInt(t[6]);



			m++;
		}
		
		jobTable.add(j);
	}



}
