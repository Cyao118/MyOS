package com.os;

import java.util.Random;

public enum InstructTypes {
        I0(0,1,"指令0","用户态计算操作语句"),
        I1(1,2,"指令1","用户态计算操作函数"),
        I2(2,2,"指令2","键盘输入变量语句"),
        I3(3,3,"指令3","屏幕显示输出变量语句"),
        I4(4,3,"指令4","磁盘文件读操作函数"),
        I5(5,4,"指令5","磁盘文件写操作函数"),
        I6(6,4,"指令6","打印操作语句");
        private int state;
        private int runtime;
        private String name;
        private String description;
        private static int instructsTypeNum = 7;
        private InstructTypes(int state,int runtime,String name,String description)
        {
            this.state =state;
            this.runtime=runtime;
            this.name=name;
            this.description=description;
        }


        public static InstructTypes getInstructByState(int state)
        {
            InstructTypes result = null;
            for (InstructTypes s : values()) {
                if (s.getState()==state) {
                    result = s;
                    break;
                }
            }
            return result;
        }

        public static int getLogicAddressByState(int state,int baseAddress)
        {
            int result = 0;
            Random random = new Random();
            switch (state)
            {
                case 0:case 2:case 3:case 6:
                {
                    result=baseAddress+1;
                    baseAddress=result;
                    break;
                }//顺序编址：地址为基地址+1
                case 1:case 4:case 5:{
                    result= 10+random.nextInt(11);
                    break;
                }//跳跃：地址为10-20随机数
                default:break;
            }
            return result;
        }
         public static boolean getOrderTypeByState(int state)
        {
            boolean result = false;
            switch (state)
            {
                case 0:case 2:case 3:case 6: {
                    result=true;
                    break;
                }
                case 1:case 4:case 5:{
                    result= false;
                    break;
                }
                    default:break;
                }
            return result;
        }
        public static int getInstructsTypeNum() {
            return instructsTypeNum;
        }

        public static void setInstructsTypeNum(int instructsTypeNum) {
            InstructTypes.instructsTypeNum = instructsTypeNum;
        }


        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getRuntime() {
            return runtime;
        }

        public void setRuntime(int runtime) {
            this.runtime = runtime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public static void main(String[] args) {

        }
}
