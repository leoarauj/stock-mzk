package br.com.mzk.stock.exception;

import io.vertx.core.http.HttpServerResponse;

/**
 * Enum com possivéis respostas do servidor e seus respectivos {@link HttpServerResponse}
 * 
 * @author Leonardo Araújo
 */
public enum ResponseMessageCode {

	SUCESSO(200, "Sucesso"),
	PRODUTO_CRIADO(201, "Produto criado com sucesso"),
	PRODUTO_ATUALIZADO(200, "Produto atualizado com sucesso"),
	PRODUTO_REMOVIDO(204, "Baixa de produto realizada com sucesso"),

	CAMPOS_OBRIGATORIOS_NAO_INFORMADOS(400, "Campos obrigatórios não informados"),
	PRODUTO_NAO_INFORMADO(400, "Produto não foi informado"),
	SERIE_PRODUTO_NULO(400, "O número de série do produto não foi informado"),
	SERIE_INVALIDO(400, "Número de série inválido"),
	NENHUM_RESULTADO_ENCONTRADO(404, "Nenhum resultado encontrado"),
	DUPLICIDADE_PRODUTO(409, "Produto já cadastrado");

	private int codeStatus;
	private String message;

	/**
	 * Constructor
	 * 
	 * @param status
	 * @param message
	 */
	private ResponseMessageCode(final int status, final String message) {
		this.codeStatus = status;
		this.message = message;
	}
	
	/**
	 * @return the status
	 */
	public int getCodeStatus() {
		return codeStatus;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}
