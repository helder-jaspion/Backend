package br.com.cdsoft.oauth.dto;

import java.io.Serializable;

/***
 * Objeto de conversão para retorno.
 * 
 * @author Cléber da Silveira.
 *
 */
public class JSONValue<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6054433144607862548L;
	private T value;
	private String key;

	public T getValue() {
		return value;
	}
	public JSONValue(final String key,final T value) {
		this.value = value;
		this.key = key;
		
	}
	public JSONValue(final T value) {
		this.value = value;
		
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
