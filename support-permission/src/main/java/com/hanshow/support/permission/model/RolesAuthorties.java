package com.hanshow.support.permission.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class RolesAuthorties {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private long roles_id;
	
	private long authorities_id;
	
	private Date createDate;
	
}
