package com.hanshow.support.permission.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Data;

@Data
@Entity
public class Users implements Serializable {
    
	/*`username` varchar(50) NOT NULL,
	  `password` varchar(255) NOT NULL,
	  `enabled` int(3) NOT NULL DEFAULT '0',
	  `create_date` timestamp NOT NULL,
	  `locale` varchar(50) DEFAULT NULL,
	  `admin` int(11) NOT NULL DEFAULT '0'*/
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false)
	private String username;
	
	@Transient
	private String oldPassword;
	
	private String password;
	
	private Boolean enabled;
	
	private String locale;
	
	private Boolean admin;
	
	@Column(name="create_date")
	private Date createDate;
}