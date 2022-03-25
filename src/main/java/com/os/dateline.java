package com.os;

public class dateline {
   public short indate;
   public short outdate;
   public dateline() {
	   indate=-1;
	   outdate=-1;
   }
   public void set_date(short in) {
	   indate=in;
   }
   public short get_date() {
	   outdate=indate;
	   return outdate;
   }
}
