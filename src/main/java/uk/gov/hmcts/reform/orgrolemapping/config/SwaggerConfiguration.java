package uk.gov.hmcts.reform.orgrolemapping.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {


    @Value("${swaggerUrl}")
    private String host;

    @Bean
    public Docket apiV2() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("v2")
            .select()
            //.apis(RequestHandlerSelectors.basePackage(TestFileName.class.getPackage().getName()))
            .build()
            .useDefaultResponseMessages(false)
            .apiInfo(apiV2Info())
            .host(host)
            .globalOperationParameters(Arrays.asList(
                headerServiceAuthorization(),
                headerAuthorization(),
                headerUserId(),
                headerUserRoles()
                                                    ));
    }

    private ApiInfo apiV2Info() {
        return new ApiInfoBuilder()
            .title("Organisation Role Mapping Service")
            .description("download, upload")
            .version("2-beta")
            .build();
    }

    private Parameter headerServiceAuthorization() {
        return new ParameterBuilder()
            .name("ServiceAuthorization")
            .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
            .modelRef(new ModelRef("string"))
            .parameterType("header")
            .required(true)
            .build();
    }

    private Parameter headerAuthorization() {
        return new ParameterBuilder()
            .name("Authorization")
            .description("Keyword `Bearer` followed by a valid IDAM user token")
            .modelRef(new ModelRef("string"))
            .parameterType("header")
            .required(true)
            .build();
    }

    private Parameter headerUserId() {
        return new ParameterBuilder()
            .name("user-id")
            .description(
                "User-id of the currently authenticated user. If provided will be used to populate the creator field "
                + "of a document and"
                + " will be used for authorisation.")
            .modelRef(new ModelRef("string"))
            .parameterType("header")
            .required(false)
            .build();
    }

    private Parameter headerUserRoles() {
        return new ParameterBuilder()
            .name("user-roles")
            .description(
                "Comma-separated list of roles of the currently authenticated user. If provided will be used for "
                + "authorisation.")
            .modelRef(new ModelRef("string"))
            .parameterType("header")
            .required(false)
            .build();
    }

}
