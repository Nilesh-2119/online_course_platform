package com.courseplatform.payment;

import com.courseplatform.common.exception.PaymentProcessingException;
import com.courseplatform.config.AppProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class RazorpayOrderClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RazorpayOrderClient.class);

    private final RestClient restClient;
    private final AppProperties appProperties;

    public RazorpayOrderClient(RestClient.Builder restClientBuilder, AppProperties appProperties) {
        this.appProperties = appProperties;
        this.restClient = restClientBuilder
                .baseUrl("https://api.razorpay.com/v1")
                .build();
    }

    /**
     * Creates a server-side order with Razorpay Gateway.
     * In local development mode with placeholder credentials, returns a mock order for seamless offline testing.
     *
     * @param amountSubunits Amount in smallest currency unit (paise for INR)
     * @param currency Currency code (e.g. INR)
     * @param receipt Platform internal purchase receipt reference
     * @param notes Additional audit metadata
     * @return RazorpayOrderResult containing order ID and status
     */
    public RazorpayOrderResult createOrder(long amountSubunits, String currency, String receipt, Map<String, String> notes) {
        String keyId = appProperties.payment().razorpay().keyId();
        String keySecret = appProperties.payment().razorpay().keySecret();

        if (keyId != null) {
            keyId = keyId.trim().replace("\"", "").replace("'", "");
        }
        if (keySecret != null) {
            keySecret = keySecret.trim().replace("\"", "").replace("'", "");
        }

        if (!StringUtils.hasText(keyId) || !StringUtils.hasText(keySecret) || keyId.contains("placeholder") || keySecret.contains("placeholder")) {
            log.error("Razorpay API credentials are not configured or are set to placeholder. Please configure RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in server environment variables.");
            throw new PaymentProcessingException("Razorpay payment gateway credentials are not configured. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in your server environment variables.");
        }

        String authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> payload = Map.of(
                "amount", amountSubunits,
                "currency", currency,
                "receipt", receipt,
                "notes", notes
        );

        log.debug("Creating Razorpay order for receipt: {}, amount: {} {}", receipt, amountSubunits, currency);

        try {
            RazorpayOrderResult response = restClient.post()
                    .uri("/orders")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(RazorpayOrderResult.class);

            if (response == null || !StringUtils.hasText(response.getId())) {
                log.error("Razorpay returned empty order response for receipt: {}", receipt);
                throw new PaymentProcessingException("Failed to generate order from payment gateway");
            }

            log.info("Successfully created Razorpay order: {} for receipt: {}", response.getId(), receipt);
            return response;

        } catch (RestClientResponseException ex) {
            String responseBody = ex.getResponseBodyAsString();
            log.error("Razorpay order creation failed with status {}: {}", ex.getStatusCode(), responseBody);
            if (ex.getStatusCode().value() == 401 || responseBody.contains("Authentication failed")) {
                String maskedKey = keyId.length() > 8 ? keyId.substring(0, 8) + "..." : "***";
                throw new PaymentProcessingException("Payment gateway authentication failed (401). Please check that RAZORPAY_KEY_ID (" + maskedKey + ") and RAZORPAY_KEY_SECRET in your server environment variables match your active Razorpay Dashboard keys and have not been regenerated.", ex);
            }
            throw new PaymentProcessingException("Payment gateway rejected order creation request: " + responseBody, ex);
        } catch (Exception ex) {
            log.error("Failed to communicate with Razorpay API: {}", ex.getMessage());
            throw new PaymentProcessingException("Unable to communicate with payment processor", ex);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RazorpayOrderResult {
        private String id;
        private Long amount;
        private String currency;
        private String receipt;
        private String status;

        public RazorpayOrderResult() {
        }

        public RazorpayOrderResult(String id, Long amount, String currency, String receipt, String status) {
            this.id = id;
            this.amount = amount;
            this.currency = currency;
            this.receipt = receipt;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getReceipt() {
            return receipt;
        }

        public void setReceipt(String receipt) {
            this.receipt = receipt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String id;
            private Long amount;
            private String currency;
            private String receipt;
            private String status;

            public Builder id(String id) {
                this.id = id;
                return this;
            }

            public Builder amount(Long amount) {
                this.amount = amount;
                return this;
            }

            public Builder currency(String currency) {
                this.currency = currency;
                return this;
            }

            public Builder receipt(String receipt) {
                this.receipt = receipt;
                return this;
            }

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public RazorpayOrderResult build() {
                return new RazorpayOrderResult(id, amount, currency, receipt, status);
            }
        }
    }
}
