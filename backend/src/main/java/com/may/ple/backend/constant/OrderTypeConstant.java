package com.may.ple.backend.constant;

public enum OrderTypeConstant {
	TYPE1(1, "3 ตัวตรง"),
	TYPE11(11, "3 ตัว กลับ 3 ตู"),
	TYPE12(12, "3 ตัว กลับ 6 ตู"),
	TYPE13(13, "3 ตัวโต๊ด แม่"),
	TYPE131(131, "3 ตัวโต๊ด ลูก"),
	TYPE14(14, "3 ตัว กลับ 3 ตู และ โต๊ด"),
	TYPE15(15, "3 ตัว กลับ 6 ตู และ โต๊ด"),
	TYPE16(16, "กลับทุกตู 4 และ 5 หลัก"),
	
	TYPE2(2, "2 ตัวบน"),
	TYPE21(21, "2 ตัวบน กลับ"),
	TYPE3(3, "2 ตัวล่าง"),
	TYPE31(31, "2 ตัวล่าง กลับ"),
	
	TYPE4(4, "ลอย");
	
	private int id;
	private String desc;
	
	private OrderTypeConstant(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}
	
	public static OrderTypeConstant findById(int id) {
		OrderTypeConstant[] values = OrderTypeConstant.values();
		for (OrderTypeConstant rolesConstant : values) {
			if(rolesConstant.getId() == id) 
				return rolesConstant;
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}
	
}
