package com.os;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

import javax.swing.*;

public class TheFrame{
	    JFrame f = new JFrame();
	  	public JPanel panel1;
	    public JPanel panel2;
	    public JPanel panel3;	
	    public JButton b1,b2,b3;
	    public JScrollPane p[]=new JScrollPane[11];
	    public JTextArea textArea[]=new JTextArea[13];
        public management control;
        public int jobnum=4;
        public static int flag=0;
	    public JFileChooser chooser;

		public TheFrame() {  
			textArea[0] = new JTextArea();
			textArea[0].setBounds(80, 30, 300, 250);
			textArea[1] = new JTextArea();
			textArea[1].setBounds(80, 300, 300, 100);
			textArea[2] = new JTextArea();
			textArea[2].setBounds(400, 30, 100, 175);
			textArea[3] = new JTextArea();
			textArea[3].setBounds(530, 30, 100, 175);
			textArea[4] = new JTextArea();
			textArea[4].setBounds(660, 30, 100, 175);
			textArea[5] = new JTextArea();
			textArea[5].setBounds(400, 230, 100, 175);
			textArea[6] = new JTextArea();
			textArea[6].setBounds(530, 230, 100, 175);
			textArea[7] = new JTextArea();
			textArea[7].setBounds(660, 230, 100, 175);
			textArea[8] = new JTextArea();
			textArea[8].setBounds(345, 30, 35, 35);
			textArea[9] = new JTextArea();
			textArea[9].setBounds(345, 70, 35, 35);
			textArea[10] = new JTextArea();
			textArea[10].setBounds(530, 230,100, 175);
			textArea[11] = new JTextArea();
			textArea[12] = new JTextArea();
			p[0] = new JScrollPane(textArea[0]);
			p[0].setBounds(25, 30, 300, 250);
			p[1] = new JScrollPane(textArea[1]);
			p[1].setBounds(25, 300, 300, 100);
			p[2] = new JScrollPane(textArea[2]);
			p[2].setBounds(400, 30, 100, 175);
			p[3] = new JScrollPane(textArea[3]);
			p[3].setBounds(530, 30, 100, 175);
			p[4] = new JScrollPane(textArea[4]);
			p[4].setBounds(660, 30, 100, 175);
			p[5] = new JScrollPane(textArea[5]);
			p[5].setBounds(400, 230, 100, 80);
			p[6] = new JScrollPane(textArea[6]);
			p[6].setBounds(660, 230, 100, 80);
			p[7] = new JScrollPane(textArea[7]);
			p[7].setBounds(660, 320, 100, 80);
			p[8] = new JScrollPane(textArea[10]);
			p[8].setBounds(400, 320,100, 80);
			p[9] = new JScrollPane(textArea[11]);
			p[9].setBounds(530, 230,100, 80);
			p[10] = new JScrollPane(textArea[12]);
			p[10].setBounds(530, 320,100, 80);
			b1=new JButton("创建新作业");
			b1.setBounds(350,420, 130, 20);
			b2=new JButton("生成作业");
			b2.setBounds(100,420, 130, 20);
			b3=new JButton("开始");
			b3.setBounds(600,420, 130, 20);
			SimpleListener outListener2 = new SimpleListener();
			b1.addActionListener(outListener2);
			b2.addActionListener(outListener2);
			b3.addActionListener(outListener2);
			f.setTitle("操作系统");
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setSize(800, 500);
			f.setLayout(null);
			f.setLocationRelativeTo(null);
			Container con = f.getContentPane();
			panel1 = new JPanel();
			f.add(p[0]);
			f.add(p[1]);
			f.add(p[2]);
			f.add(p[3]);
			f.add(p[4]);
			f.add(p[5]);
			f.add(p[6]);
			f.add(p[7]);
			f.add(textArea[8]);
			f.add(textArea[9]);
			f.add(p[8]);
			f.add(p[9]);
			f.add(p[10]);
			f.add(b1);
			f.add(b2);
			f.add(b3);
			con.add(panel1);
			f.setVisible(true);
		}
		public void add() throws IOException {
		    	File fp;
		    	JCB job=new JCB();
		    	fp = new File("19317124-jobs-input.txt");
		    	BufferedWriter output = new BufferedWriter(new FileWriter(fp,true));
				 //output.write( "Instruc_ID\tInstruc_State\tName\n");
				 jobnum++;
				 Random r = new Random();
				 int ran1 = r.nextInt(jobnum);
					job.priority = ran1+1;
					job.arriveTime =management.nowTime+5;
					job.instructNum = r.nextInt(11)+20;
					job.jobid=jobnum;
					job.d1=0;
					job.d2=0;
					job.psw=1;
					job.IR = new Instruct[job.instructNum];
					for(int i = 0 ;i < job.instructNum; i++) {
						job.IR[i] = new Instruct();
					}
					//instruc_num[i]= instrucnum;
					output.write(jobnum+"\t"+job.priority+"\t"+job.arriveTime+"\t"+job.d1+"\t"+job.d2+"\t"+job.instructNum+"\n");
					String name=jobnum+".txt";
					fp = new File(name);
					if(!fp.exists())
						fp.createNewFile();  //创建文件
					else {  //清空文件
						FileWriter fw = new FileWriter(fp);
						fw.write("");
						fw.close();
					}
					output = new BufferedWriter(new FileWriter(fp,true));
					output.write( "Instruc_ID\tInstruc_State\tName\n");
				    int Instruc_State = 0;
				  
					  int i = 0;
					  r = new Random();
					  for (;i < job.instructNum;i++) {
						  job.IR[i].Instruct_ID =i;
						  Instruc_State = r.nextInt(12);
						  job.IR[i].Instruct_State = Instruc_State;
						output.write(i+1+"\t\t"+Instruc_State+"\t\t\n");
					  }
					  output.close();
					  management.jobTable.add(job);
		           
		}
		public void creat() throws IOException {
			CreatJobs one=new CreatJobs();
			one.set_num(4);
			one.creat_job();
			one.creat_Instruc();
			flag=1;
			
		}
		public void begin() throws IOException {
			 //System.out.println("开始运行！！！！！");
			 management control = new management();
		}
		private class SimpleListener implements ActionListener {
				public SimpleListener() {
				}

				public void actionPerformed(ActionEvent e) {
					String buttoname = e.getActionCommand();
					if (buttoname.equals("创建新作业")) {
						try {
							add();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					if (buttoname.equals("生成作业")) {
					   try {
						creat();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					}
					if (buttoname.equals("开始")) {
						   try {
							begin();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						}
				}
			}
	

}
