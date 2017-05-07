package com.firebase.dao;

import java.util.List;

public class Person {
	
	//String id = UUID.randomUUID().toString();
	String firstName;
	String lastName;
	String contactNo;
	String email;
	List<Complaint> complaints;

    public List<Complaint> getComplaints() {
        return complaints;
    }

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    /*public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }*/
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getContactNo() {
		return contactNo;
	}
	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
	}
	/*public Map<String, Complaint> getComplaints() {
		return complaints;
	}
	public void setComplaints(Map<String, Complaint> complaints) {
		this.complaints = complaints;
	}
	
	*/
	
}
