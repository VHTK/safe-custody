package com.bourne.result;

import com.bourne.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public class Result<T> implements Serializable {

	private static final long serialVersionUID = 5758020086424746744L;

	public static final String SUCCESS_CODE = "0";

	private String code;
	private String message;
	private T result;
	private Class<T> resultType;

	public static Object fail(String s, String toString) {
		Result<String> result = new Result<>();
		result.setResult(s);
		result.setMessage(toString);
		return result;
	}

	public static Object info(String code, String s, String message) {
		Result<String> result = new Result<>();
		result.setCode(code);
		result.setResult(s);
		result.setMessage(message);
		return result;
	}

	public boolean failed() {
		return !success();
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getResult() {
		return result;
	}

	public Class<T> getResultType() {
		return resultType;
	}

	public Result<T> setCode(String code) {
		this.code = code;
		return this;
	}

	public Result<T> setCodeSuccess() {
		this.code = SUCCESS_CODE;
		return this;
	}

	public Result<T> setMessage(String message) {
		this.message = message;
		return this;
	}

	/**
	 * It's better to invoke setSuccess() or setFail() instead of this method<br>
	 * This method is invoked by Feign
	 * 
	 * @param result
	 * @return
	 */
	public Result<T> setResult(T result) {
		this.result = result;
		return this;
	}

	public Result<T> setResultType(Class<T> resultType) {
		this.resultType = resultType;
		return this;
	}

	@SuppressWarnings("unchecked")
	public Result<T> setSuccess(T result) {
		setCodeSuccess();
		if (result != null) {
			this.resultType = (Class<T>) result.getClass();
		}
		this.result = result;
		return this;
	}

	@SuppressWarnings("unchecked")
	public Result<T> setFail(String code, String message, T result) {
		this.setCode(code).setMessage(message);
		if (result != null) {
			this.resultType = (Class<T>) result.getClass();
		}
		this.result = result;
		return this;
	}

	public boolean success() {
		return SUCCESS_CODE.equals(code);
	}

	/**
	 * Try to convert result to the real type.
	 * 
	 * @return
	 */
	@JsonIgnore
	public T getTypedResult() {
		if (result != null) {
			if (!result.getClass().equals(resultType)) {
				if (resultType != null) {
					final String jsonString = JsonUtil.toJson(result);
					return JsonUtil.fromJson(jsonString, resultType);
				}
			} else {
				return result;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FlowResult [code=").append(code).append(", message=").append(message).append(", result=")
				.append(result).append(", resultType=").append(resultType).append("]");
		return builder.toString();
	}

}
