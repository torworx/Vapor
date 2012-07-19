package com.acme;

import evymind.vapor.core.utils.component.AbstractLifecycle;

public class Base extends AbstractLifecycle {
	
	String name;
	int value;
	String[] messages;


	/**
	 * @return Returns the messages.
	 */
	public String[] getMessages() {
		return messages;
	}


	/**
	 * @param messages
	 *            The messages to set.
	 */
	public void setMessages(String[] messages) {
		this.messages = messages;
	}


	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return Returns the value.
	 */
	public int getValue() {
		return value;
	}


	/**
	 * @param value
	 *            The value to set.
	 */
	public void setValue(int value) {
		this.value = value;
	}


	public void doSomething(int arg) {
		System.err.println("doSomething " + arg);
	}


	public String findSomething(int arg) {
		return ("found " + arg);
	}

}
