package br.com.mzk.stock.server;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.mzk.stock.exception.ResponseMessageCode;
import br.com.mzk.stock.exception.ResponseMessageException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class Server extends AbstractVerticle {
	private HttpServer server;
	
	private JsonArray produtos = new JsonArray();
	
	private static final AtomicInteger COUNTER = new AtomicInteger(1);


	/**
	 * Retorna a lista de produtos cadastrados
	 * 
	 * @param routingContext
	 */
	private void getAll(RoutingContext routingContext) {
		sendSuccessAndArray(ResponseMessageCode.SUCESSO, routingContext.response(), produtos);
	}

	/**
	 * Remove um Produto pelo número de série da lista de produtos
	 * 
	 * @param routingContext
	 */
	private void delete(RoutingContext routingContext) {
		String id = routingContext.request().getParam("id");

		try {
			if(id == null || id.isBlank()) {
				throw new ResponseMessageException(ResponseMessageCode.SERIE_PRODUTO_NULO);
			}

			Long idAsLong = Long.valueOf(id);
			JsonObject produto = getProdutoById(idAsLong);

			if(produto == null) {
				throw new ResponseMessageException(ResponseMessageCode.SERIE_INVALIDO);
			}

			produtos.remove(produto);

			sendSuccess(ResponseMessageCode.SUCESSO, routingContext.response());

		} catch (ResponseMessageException e) {
			sendError(e, routingContext.response());

		} catch (NumberFormatException e) {
			sendError(ResponseMessageCode.SERIE_INVALIDO, routingContext.response());
		}
	}

	/**
	 * Adiciona o produto a lista de produtos
	 * 
	 * @param routingContext
	 */
	private void addProduto(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();

		try {
			JsonObject produto = routingContext.getBodyAsJson();

			if(produto.isEmpty()) {
				throw new ResponseMessageException(ResponseMessageCode.PRODUTO_NAO_INFORMADO);
			}

			validarCamposObrigatorios(produto);
			validarDuplicidade(produto);

			produto.put("id", COUNTER.getAndIncrement());

			produtos.add(produto);

			sendSuccessAndObject(ResponseMessageCode.PRODUTO_CRIADO, response, produto);
		} catch (ResponseMessageException e) {
			sendError(e, response);
		}
	}

	/**
	 * Retorna o Produto segundo id informado
	 * 
	 * @param serie
	 * @return
	 */
	private JsonObject getProdutoById(final Long id) {
		Object produto = produtos.stream().filter(p -> {
			JsonObject produtoOfList = new JsonObject(p.toString());
			Long idOfList = produtoOfList.getLong("id");

			if(id.compareTo(idOfList) == 0) {
				return true;
			}

			return false;
		}).findFirst().orElse(null);

		return produto == null ? null : new JsonObject(produto.toString());
	}

	/**
	 * Realiza a verificação de duplicidade de produto por Codigo de barras e numero de serie
	 * @param produto
	 */
	private void validarDuplicidade(JsonObject produto) {
		Long serieNovo = produto.getLong("serie");
		Long codigoBarraNovo = produto.getLong("codigoBarra");

		Object produtoDuplicado = produtos.stream().filter(p -> {
			JsonObject produtoOfList = new JsonObject(p.toString());
			Long serieOfList = produtoOfList.getLong("serie");
			Long codigoBarraOfList = produtoOfList.getLong("codigoBarra");

			if(serieNovo.compareTo(serieOfList) == 0 && codigoBarraNovo.compareTo(codigoBarraOfList) == 0) {
				return true;
			}

			return false;
		}).findFirst().orElse(null);

		if(produtoDuplicado != null) {
			throw new ResponseMessageException(ResponseMessageCode.DUPLICIDADE_PRODUTO);
		}
	}

	/**
	 * Realiza a validação de campos obrigatórios
	 * 
	 * @param produto
	 */
	private void validarCamposObrigatorios(JsonObject produto) {
		String nome = produto.getString("nome");
		Long codigoBarra = produto.getLong("codigoBarra");
		Long serie = produto.getLong("serie");

		if(nome == null 
				|| nome.isBlank()
				|| codigoBarra == null
				|| codigoBarra.compareTo(BigInteger.ZERO.longValue()) <= 0
				|| serie == null
				|| serie.compareTo(BigInteger.ZERO.longValue()) <= 0) {

			throw new ResponseMessageException(ResponseMessageCode.CAMPOS_OBRIGATORIOS_NAO_INFORMADOS);
		}
	}
	
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router router = Router.router(vertx);
		
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.setStatusCode(200);
			response.putHeader("content-type", "text/html").end("<h1> MZK </h1>");
		});
		
		router.route("/produto*").handler(BodyHandler.create());
		router.post("/produto").handler(this::addProduto);
		router.get("/produto").handler(this::getAll);
		router.delete("/produto/:id").handler(this::delete);
		
		server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
		
		server.requestHandler(router).listen(ar -> {
			if(ar.succeeded()) {
				startPromise.complete();
				System.out.println("Servidor iniciado na porta 8080");
			}
			else startPromise.fail(ar.cause());
		});
	}

	/**
	 * Recebe {@link ResponseMessageException} e retorna o Status Http e uma mensagem de erro como resposta a requisição
	 * 
	 * @param statusCode
	 * @param response
	 * @param msg
	 */
	private void sendError(ResponseMessageException exception, HttpServerResponse response) {
		response.setStatusCode(exception.getStatus()).end(exception.getMessage());
	}

	/**
	 * Recebe {@link ResponseMessageCode} e retorna o Status Http e uma mensagem de erro como resposta a requisição
	 * 
	 * @param responseMessage
	 * @param response
	 */
	private void sendError(ResponseMessageCode responseMessage, HttpServerResponse response) {
		response.setStatusCode(responseMessage.getCodeStatus()).end(responseMessage.getMessage());
	}

	/**
	 * Em caso de sucesso retorna o Status Http, uma mensagem de sucesso e um Objeto no corpo da requisição
	 * 
	 * @param code
	 * @param routingContext
	 * @param produto
	 */
	private void sendSuccessAndObject(ResponseMessageCode code, HttpServerResponse response, JsonObject produto) {
		response.setStatusCode(code.getCodeStatus())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(produto == null ? new JsonObject().encode() : produto.encode());
	}

	/**
	 * Em caso de sucesso retorna o Status Http, uma mensagem de sucesso e uma lista de objetos no corpo da requisição
	 * 
	 * @param code
	 * @param response
	 * @param jsonArray
	 */
	private void sendSuccessAndArray(ResponseMessageCode code, HttpServerResponse response, JsonArray jsonArray) {
		response.setStatusCode(code.getCodeStatus())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(jsonArray == null ? new JsonArray().encode() : jsonArray.encode());
	}

	/**
	 * Em caso de sucesso retorna o Status Http
	 * 
	 * @param code
	 * @param response
	 */
	private void sendSuccess(ResponseMessageCode code, HttpServerResponse response) {
		response.setStatusCode(code.getCodeStatus())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end();
	}
}














//private Promise<Void> startHttpServer() {
//	Promise<Void> promise = Promise.promise();
//
//	OpenAPI3RouterFactory.create(vertx, "/openapi.json", openAPI3RouterFactoryAsyncResult -> {
//		if (openAPI3RouterFactoryAsyncResult.succeeded()) {
//			OpenAPI3RouterFactory routerFactory = openAPI3RouterFactoryAsyncResult.result();
//
//			routerFactory.mountServicesFromExtensions();
//
//			Router router = routerFactory.getRouter();
//			server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("localhost"));
//
//			server.requestHandler(router).listen(ar -> {
//				if(ar.succeeded()) promise.complete();
//				else promise.fail(ar.cause());
//			});
//		} else {
//			promise.fail(openAPI3RouterFactoryAsyncResult.cause());
//		}
//	});
//
//	return promise;
//}
