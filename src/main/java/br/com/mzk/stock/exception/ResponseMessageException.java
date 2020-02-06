package br.com.mzk.stock.exception;

import java.time.LocalDateTime;

/**
 * Classe de resposta para tratamento de {@link Exception} seguindo o padrão {@link HttpResponseMessage}
 * 
 * @author Leonardo Araújo
 */
public class ResponseMessageException extends RuntimeException {
	private static final long serialVersionUID = 7559593388030191880L;

	private int status;
	private String message;
	private LocalDateTime date;

	/**
	 * Construtor padrão
	 * @param restMessageCode
	 */
	public ResponseMessageException(final ResponseMessageCode responseMessageCode) {
		this.status = responseMessageCode.getCodeStatus();
		this.message = responseMessageCode.getMessage();
		this.date = LocalDateTime.now();
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the date
	 */
	public LocalDateTime getDate() {
		return date;
	}

}
