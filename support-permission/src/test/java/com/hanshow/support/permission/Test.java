package com.hanshow.support.permission;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import junit.framework.TestCase;

public class Test extends TestCase {



	@org.junit.Test
	public void test() {
		// TODO Auto-generated method stub
		
		System.out.println(new BCryptPasswordEncoder().encode("123456"));
	}

}
