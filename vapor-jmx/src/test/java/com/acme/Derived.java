package com.acme;

public class Derived extends Base implements Signature {
	String fname = "Full Name";

	public String getFullName() {
		return fname;
	}

	public void setFullName(String name) {
		fname = name;
	}

	public void publish() {
		System.err.println("publish");
	}

	public void doodle(String doodle) {
		System.err.println("doodle " + doodle);
	}

	public void somethingElse() {

	}
}
