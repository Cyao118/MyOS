package com.os;

import java.io.*;
import java.util.*;

public class CreatJobs {
	public int PCB_num=0;
	public int instruc_num[]=new int[100];
	public boolean if_creatprocess;
	public int resource1[]=new int[100];
	public int resource2[]=new int[100];
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
		fp = new File("19317124-jobs-input.txt");
		
			if(!fp.exists())
				fp.createNewFile();  //创建文件
			else {  //清空文件
				FileWriter fw = new FileWriter(fp);
				fw.write("");
				fw.close();
			}
			if (PCB_num != 0 && !if_creatprocess) {	
			BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
	        output.write("ProID\tPriority\tInTime(s)\tresource1\tresource2\tInstrucNum \n");
	        int i =0;
	        Random r = new Random();
			for (;i < PCB_num;i++) {
			    int ran1 = r.nextInt(PCB_num);
				priority = ran1+1;
				midtime =r.nextInt(6)+5;
				instrucnum = r.nextInt(11)+20;
				resource1[i]=0;
				resource2[i]=0;
				instruc_num[i]= instrucnum;
				output.write(i+1+"\t"+priority+"\t"+intime+"\t"+resource1[i]+"\t"+resource2[i]+"\t"+instrucnum+"\n");
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
			 output.write( "Instruc_ID\tInstruc_State\tName\n");
		    int Instruc_State = 0;
		    if (PCB_num != 0 && !if_creatInstruc[j]) {
			  int i = 0;
			  Random r = new Random();
			  for (;i < instruc_num[j];i++) {
				Instruc_State = r.nextInt(12);
				output.write(i+1+"\t\t"+Instruc_State+"\t\t\n");
				/*switch(Instruc_State) {
				case 0:output.write("do"+"\n");break;
				case 1:output.write("needfirst"+"\n");break;
				case 2:output.write("freefirst"+"\n");break;
				case 3:output.write("needblock"+"\n");break;
				case 4:output.write("P(tomutex)"+"\n");break;
				case 5:output.write("V(freemutex)"+"\n");break;
				case 6:output.write("P(tofull)"+"\n");break;
				case 7:output.write("V(freefull)"+"\n");break;
				case 8:output.write("P(toempty)"+"\n");break;
				case 9:output.write("V(freeempty)"+"\n");break;
				case 10:output.write("needsed"+"\n");break;
				case 11:output.write("freesed"+"\n");break;
				}*/
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
