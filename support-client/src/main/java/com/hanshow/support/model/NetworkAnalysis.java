package com.hanshow.support.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="network_analysis")
public class NetworkAnalysis implements Serializable {

	private static final long serialVersionUID = 9202061046803260650L;
	/*`id` Integer PRIMARY KEY NOT NULL,
    `store_code` INTEGER NOT NULL,
    `log_time` DATETIME NOT NULL,
    `esl_id` VARCHAR(32) NOT NULL,
    `rf_power` INTEGER NOT NULL,
    `ap_id` INTEGER NOT NULL,
    `task_id` INTEGER NOT NULL*/
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name="log_time")
	private Date logTime;
	
	@Column(name="store_code")
	private String storeCode;
	
	@Column(name="esl_id")
	private String eslId;
	
	@Column(name="rf_power")
	private Integer rfPower;
	
	@Column(name="ap_id")
	private Integer apId;
	
	@Column(name="task_id")
	private Long taskId;
	
	@Column(name="esl_count")
	private Long eslCount;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getLogTime() {
		return logTime;
	}

	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getEslId() {
		return eslId;
	}

	public void setEslId(String eslId) {
		this.eslId = eslId;
	}

	public Integer getRfPower() {
		return rfPower;
	}

	public void setRfPower(Integer rfPower) {
		this.rfPower = rfPower;
	}

	public Integer getApId() {
		return apId;
	}

	public void setApId(Integer apId) {
		this.apId = apId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getEslCount() {
		return eslCount;
	}

	public void setEslCount(Long eslCount) {
		this.eslCount = eslCount;
	}

}
