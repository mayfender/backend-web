package com.may.ple.backend.constant;

public enum OrderTypeConstant {
	TYPE1(1, "3 ตัวตรง"),
	TYPE11(11, "3 ตัว กลับ"),
	TYPE12(12, "กลับทุกตู 4 และ 5 ตัว"),
	TYPE13(13, "3 ตัวโต๊ด แม่"),
	TYPE131(131, "3 ตัวโต๊ด ลูก"),
	TYPE132(132, "เฉพาะ 3 ตัวโต๊ด แม่"),
	TYPE14(14, "3 ตัว กลับ และ โต๊ด"),

	TYPE2(2, "2 ตัวบน"),
	TYPE21(21, "2 ตัวบน กลับ"),
	TYPE3(3, "2 ตัวล่าง"),
	TYPE31(31, "2 ตัวล่าง กลับ"),

	TYPE4(4, "ลอย 1 ตัว"),
	TYPE41(41, "ลอยแพ 4 ตัว"),
	TYPE42(42, "ลอยแพ 5 ตัว"),
	TYPE43(43, "วิ่งบน"),
	TYPE44(44, "วิ่งล่าง"),

	TYPE50(60, "ปักบน หลัก หน่วย"),
	TYPE51(61, "ปักบน หลัก สิบ"),
	TYPE52(62, "ปักบน หลัก ร้อย"),

	TYPE53(63, "ปักล่าง หลัก หน่วย"),
	TYPE54(64, "ปักล่าง หลัก สิบ");

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
