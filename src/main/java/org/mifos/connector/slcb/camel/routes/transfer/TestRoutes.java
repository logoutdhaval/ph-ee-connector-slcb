package org.mifos.connector.slcb.camel.routes.transfer;

import org.apache.camel.builder.RouteBuilder;
import org.mifos.connector.slcb.config.AwsFileTransferService;
import org.mifos.connector.slcb.dto.PaymentRequestDTO;
import org.mifos.connector.slcb.utils.CsvUtils;
import org.mifos.connector.slcb.zeebe.ZeebeProcessStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

import static org.mifos.connector.slcb.camel.config.CamelProperties.SLCB_CHANNEL_REQUEST;

@Component
public class TestRoutes extends RouteBuilder {

    private final ZeebeProcessStarter zeebeProcessStarter;

    private final AwsFileTransferService fileTransferService;

    public TestRoutes(ZeebeProcessStarter zeebeProcessStarter, AwsFileTransferService fileTransferService) {
        this.zeebeProcessStarter = zeebeProcessStarter;
        this.fileTransferService = fileTransferService;
    }

    @Override
    public void configure() throws Exception {
        from("rest:get:/start")
                .id("slcb-flow-start")
                .process(exchange -> {
                    try {
                        Map<String, Object> variables = new HashMap<>();
                        variables.put("transactionId", UUID.randomUUID());
                        zeebeProcessStarter.startZeebeWorkflow("SLCB", variables);
                    } catch (Exception e) {

                    }

                })
                .setBody(constant("Started"));

        from("rest:get:/test/file")
                .id("file-upload-service")
                .process(exchange -> {
                    List<PaymentRequestDTO> paymentRequestDTOList = new ArrayList<>();

                    PaymentRequestDTO paymentRequestDTO1 = new PaymentRequestDTO();
                    paymentRequestDTO1.setId("0");
                    paymentRequestDTO1.setAccountType(0);
                    paymentRequestDTO1.setAuthorizationCode("asd");

                    PaymentRequestDTO paymentRequestDTO2 = new PaymentRequestDTO();
                    paymentRequestDTO2.setId("1");
                    paymentRequestDTO2.setAccountType(1);
                    paymentRequestDTO2.setAuthorizationCode("demo auth code");

                    paymentRequestDTOList.add(paymentRequestDTO1);
                    paymentRequestDTOList.add(paymentRequestDTO2);


                    File file = CsvUtils.createCSVFile(paymentRequestDTOList, PaymentRequestDTO.class);
                    System.out.println(file.getAbsolutePath());
                    String result = fileTransferService.uploadFile(file);
                    exchange.getIn().setBody(result);
                });

        from("rest:post:test/transferRequest")
                .process(exchange -> exchange.setProperty(SLCB_CHANNEL_REQUEST, exchange.getIn().getBody(String.class)))
                .to("direct:transfer-route")
                .setBody(exchange -> exchange.getIn().getBody(String.class));
    }
}
