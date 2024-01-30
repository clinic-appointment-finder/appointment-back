package cl.hcs.finder.appointmentback.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import reactor.core.publisher.Mono;

public class Helper {

    private static final Logger log = Helper.getLogger();

    public static Logger getLogger() {
        // Obtiene automáticamente el nombre de la clase que llama a este método
        String className = new Throwable().getStackTrace()[1].getClassName();
        return LoggerFactory.getLogger(className);
    }

    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuffer buffer = new StringBuffer();
            buffer.append(String.format("<Request>: %s %s", clientRequest.method(), clientRequest.url())).append("\n");
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(
                            value -> buffer.append(String.format("<Request> %s: %s", name, value)).append("\n")));
             
            log.info(buffer.toString());
            return Mono.just(clientRequest);
        });
    }
    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            StringBuffer buffer = new StringBuffer();
            buffer.append(String.format("<Response> Status: %s", clientResponse.statusCode())).append("\n");
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values
                    .forEach(value -> buffer.append(String.format("<Response> %s: %s", name, value)).append("\n")));
            log.info(buffer.toString());
            return Mono.just(clientResponse);
        });
    }

}
