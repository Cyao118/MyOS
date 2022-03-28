package com.os;

import java.io.*;
import java.util.*;

public class CreatJobs {
	public int PCB_num=0;
	public int instruc_num[]=new int[100];
	public boolean if_creatprocess;
	public boolean if_creatInstruc[]=new boolean[100];
	public File fp;
	public int Creat_time = 0;
	public void set_num(int num) {
		PCB_num = num;
	}
	public void set_time(int time) {
		Creat_time = time;
	}
	public int creat_job() throws IOException {
		int priority = 0;
		int intime = Creat_time;
		int midtime = 0;
		int instrucnum = 0;
		fp = new File(common.user+"-jobs-input.txt");
		
			if(!fp.exists())
				fp.createNewFile();  //创建文件
			else {  //清空文件
				FileWriter fw = new FileWriter(fp);
				fw.write("");
				fw.close();
			}
			if (PCB_num != 0 && !if_creatprocess) {	
			BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
	        output.write("JobsID\tPriority\tInTimes\tInstrucNum \n");
	        int i =0;
	        Random r = new Random();
			for (;i < PCB_num;i++) {
			    int ran1 = r.nextInt(PCB_num);
				priority = ran1+1;
				midtime =r.nextInt(6)+5;
				instrucnum = r.nextInt(11)+20;
				instruc_num[i]= instrucnum;
				output.write(i+1+"\t"+priority+"\t"+intime+"\t"+instrucnum+"\n");
				intime += midtime;
			}
			output.close();
			if_creatprocess = true;
			return 1;
		}
		else {
			
	        return -1;
		}
			
	}
	public int creat_Instruc() throws IOException {
		for (int j = 0;j < PCB_num;j++) {
			int nameuse=j+1;
			String name=nameuse+".txt";
			fp = new File(name);
			if(!fp.exists())
				fp.createNewFile();  //创建文件
			else {  //清空文件
				FileWriter fw = new FileWriter(fp);
				fw.write("");
				fw.close();
			}
			BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
			 output.write( "Instruc_ID\tInstruc_State\tL_Address\tInRunTimes\n");
		    int Instruc_State = 0;
		    if (PCB_num != 0 && !if_creatInstruc[j]) {
			  int i = 0;
			  int baseAddress = -1;
			  Random r = new Random();
			  for (;i < instruc_num[j];i++) {
				Instruc_State = r.nextInt(common.instructTypeNum);
				int L_Address =InstructTypes.getLogicAddressByState(Instruc_State, baseAddress);
				output.write(i+1+"\t\t"+Instruc_State+"\t\t"+L_Address+"\t\t"+InstructTypes.getInstructByState(Instruc_State).getRuntime()+"\n");
				if(InstructTypes.getOrderTypeByState(Instruc_State))
				  	baseAddress = L_Address;
			  }
			  output.close();
				if_creatInstruc[j] = true;
		    } 
			else {
		
			}


		}
		
		return 1;
		
	}
	//public static void main(String[] args) throws IOException{
		
	//}
}
