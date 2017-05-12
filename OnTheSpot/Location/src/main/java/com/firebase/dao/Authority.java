package com.firebase.dao;

public class Authority {
	
	String name;
	String email;
	String contact;
	String zipHandled[] = new String[50];

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

	public String[] getZipHandled() {
		return zipHandled;
	}

	public void setZipHandled(String[] zipHandled) {
		this.zipHandled = zipHandled;
	}
}
