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

	public T getValue() {
		return value;
	}

	public JSONValue(T value) {
		this.value = value;
	}

}
