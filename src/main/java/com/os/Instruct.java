package com.os;

import com.oracle.webservices.internal.api.databinding.DatabindingMode;



import java.util.Random;
//根据地址线的大小确定指令为16位
public class Instruct {
    public int time=1;  //指令时间,每1s执行一条指令
    int []Instructsize=new int[16];
    public int Instruct_ID; //指令编号
    public int Instruct_State;//指令类型

    public int getRunedtime() {
        return runedtime;
    }

    public void setRunedtime(int runedtime) {
        this.runedtime = runedtime;
    }

    public int runedtime = 0;
    public int getL_Address() {
        return L_Address;
    }

    public int L_Address;  //指令地址
    public Instruct() {
    	this.Instruct_ID = -1;
		this.Instruct_State = -1;
		this.L_Address = -1;
    }
    public void setir(int id, int state, int add,int runedtime) {
		this.Instruct_ID = id;
		this.Instruct_State = state;
		this.L_Address = add;
		this.runedtime = runedtime;
	}
    public Instruct get_Instruct() {
    	return this;
    }
    public int get_State() {
    	return this.Instruct_State;
    }
    public int get_ID() {
    	return this.Instruct_ID;
    }
    public void set_State(int State) {
    	this.Instruct_State=State;
    }
    public void set_ID(int ID) {
    	this.Instruct_ID=ID;
    }
    public void Creat_State() {
    	Random rand= new Random();
    	this.Instruct_State=rand.nextInt(6);
    }
	public void clear() {//指令删除
		this.Instruct_ID = -1;
		this.Instruct_State = -1;
		this.L_Address = -1;
	}
}
