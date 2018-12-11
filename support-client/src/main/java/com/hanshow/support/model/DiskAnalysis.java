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
@Table(name="disk_analysis")
public class DiskAnalysis implements Serializable {

	private static final long serialVersionUID = -1206663367284350204L;

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
	
	@Column(name="session_type")
	private String sessionType;
	
	@Column(name="frame_size")
	private Long frames;
	
	@Column(name="retry_times")
	private Byte retryTimes;
	
	@Column(name="esl_count")
	private Long eslCount;
	
	@Column(name="spent_time")
	private Long spentTime;
	
	public DiskAnalysis() {}
	
	public DiskAnalysis(String storeCode, String sessionType, Long frames, Long spentTime, Long eslCount) {
		this.storeCode = storeCode;
		this.sessionType = sessionType;
		this.frames = frames;
		this.spentTime = spentTime;
		this.eslCount = eslCount;
	}

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

	public String getSessionType() {
		return sessionType;
	}

	public void setSessionType(String sessionType) {
		this.sessionType = sessionType;
	}

	public Long getFrames() {
		return frames;
	}

	public void setFrames(Long frames) {
		this.frames = frames;
	}

	public Byte getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(Byte retryTimes) {
		this.retryTimes = retryTimes;
	}

	public Long getEslCount() {
		return eslCount;
	}

	public void setEslCount(Long eslCount) {
		this.eslCount = eslCount;
	}

	public Long getSpentTime() {
		return spentTime;
	}

	public void setSpentTime(Long spentTime) {
		this.spentTime = spentTime;
	}

}
