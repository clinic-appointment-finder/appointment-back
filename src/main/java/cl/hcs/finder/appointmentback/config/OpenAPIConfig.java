package cl.hcs.finder.appointmentback.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

  @Value("${swagger.openapi.dev-url}")
  private String devUrl;

  @Value("${swagger.openapi.prod-url}")
  private String prodUrl;
    
  @Value("${swagger.openapi.url.portfolio}")
  private String urlPortfolio;

  @Bean
  public OpenAPI myOpenAPI() {
    Server devServer = new Server();
    devServer.setUrl(devUrl);
    devServer.setDescription("Server URL in Development environment");

    Server prodServer = new Server();
    prodServer.setUrl(prodUrl);
    prodServer.setDescription("Server URL in Production environment");

    Contact contact = new Contact();
    contact.setEmail("hugomode@gmail.com");
    contact.setName("Hugo Cárcamo");
    contact.setUrl(urlPortfolio);

    License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

    Info info = new Info()
        .title("Appointment BACK")
        .version("1.0")
        .contact(contact)
        .description("Esta API expone los endpoint para registrar tareas programadas para una Clínica de Chile.")
        .license(mitLicense);

    return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
  }
}