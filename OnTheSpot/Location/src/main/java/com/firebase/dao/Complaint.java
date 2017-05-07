package com.firebase.dao;

import java.util.Date;
import java.util.UUID;

public class Complaint {
	
	String id = UUID.randomUUID().toString();
	String description;
	Date complaintDate;
	String complaintImage;
	ComplaintAddress complaintLocation = new ComplaintAddress();
	String authorityName;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getComplaintDate() {
		return complaintDate;
	}
	public void setComplaintDate(Date complaintDate) {
		this.complaintDate = complaintDate;
	}
	public String getComplaintImage() {
		return complaintImage;
	}
	public void setComplaintImage(String complaintImage) {
		this.complaintImage = complaintImage;
	}
	public ComplaintAddress getComplaintLocation() {
		return complaintLocation;
	}
	public void setComplaintLocation(ComplaintAddress complaintLocation) {
		this.complaintLocation = complaintLocation;
	}
	public String getAuthorityName() {
		return authorityName;
	}
	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
	}
	
	
	
	
}
