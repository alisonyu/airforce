package com.alisonyu.airforce.web.exception;

/**
 * 封装错误信息
 * @author yuzhiyi
 * @date 2018/9/28 22:27
 */
public class ErrorMessage {

	private String path;
	private String method;
	private String time;
	private int httpCode;
	private String errorMessage;


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}


	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}


	@Override
	public String toString() {
		return "ErrorMessage{" +
				"path='" + path + '\'' +
				", time='" + time + '\'' +
				", httpCode=" + httpCode +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}
}
