package com.backend.controller;

import com.backend.dto.PaymentRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentsController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsController.class);

    @Value("${payment-processor-url.default}")
    private String defaultProcessorUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/payments")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequestDto paymentRequest) {
        logger.info("Recebida requisição de pagamento: correlationId={}, amount={}",
                paymentRequest.getCorrelationId(), paymentRequest.getAmount());

        // Montar URL com /payments
        String url = defaultProcessorUrl.endsWith("/") ? defaultProcessorUrl + "payments" : defaultProcessorUrl + "/payments";

        // Montar o corpo da requisição como Map
        Map<String, Object> body = new HashMap<>();
        body.put("correlationId", paymentRequest.getCorrelationId());
        body.put("amount", paymentRequest.getAmount());

        // requestedAt no formato ISO 8601 UTC com Z
        String requestedAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        body.put("requestedAt", requestedAt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // Converter Map para JSON String para o RestTemplate
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            logger.info("Pagamento processado com sucesso: correlationId={}, status={}",
                    paymentRequest.getCorrelationId(), response.getStatusCodeValue());

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception ex) {
            logger.error("Erro ao conectar com o processador de pagamentos: correlationId={}, erro={}",
                    paymentRequest.getCorrelationId(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Erro ao conectar com o processador de pagamentos.");
        }
    }
    @GetMapping("/payments-summary")
    public ResponseEntity<Map<String, Map<String, Object>>> getPaymentsSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        Map<String, Object> defaultSummary = Map.of(
                "totalRequests", 0,
                "totalAmount", 0
        );

        Map<String, Object> fallbackSummary = Map.of(
                "totalRequests", 0,
                "totalAmount", 0
        );

        Map<String, Map<String, Object>> result = Map.of(
                "default", defaultSummary,
                "fallback", fallbackSummary
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/purge-payments")
    public ResponseEntity<Void> purgePayments() {
        return ResponseEntity.ok().build();
    }
}
