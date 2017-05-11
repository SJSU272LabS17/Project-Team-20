package com.firebase.dao;

public class Authority {
	
	String name;
	String email;
	String contact;
	String concerns[] = new String[50];
	ComplaintAddress address = new ComplaintAddress();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String[] getConcerns() {
		return concerns;
	}
	public void setConcerns(String[] concerns) {
		this.concerns = concerns;
	}
	public ComplaintAddress getAddress() {
		return address;
	}
	public void setAddress(ComplaintAddress address) {
		this.address = address;
	}
	
	
}
