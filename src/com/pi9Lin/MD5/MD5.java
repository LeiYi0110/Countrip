package com.pi9Lin.MD5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public String getMD5(String val) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(val.getBytes());
		byte[] m = md5.digest();// 加密
		return getString(m);
	}
	//16进制 32位
	private String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			int digital = b[i];
			if(digital < 0) {
				digital += 256;
			}
			if(digital < 16){
				sb.append("0");
			}
			sb.append(Integer.toHexString(digital));
		}
		
		return sb.toString();
	}
}
