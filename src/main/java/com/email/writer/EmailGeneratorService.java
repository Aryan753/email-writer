package com.email.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                   @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey =geminiApiKey;
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        //build prompt
        String prompt=buildPrompt(emailRequest);
        //prepare raw jason body
        String requestBody=String.format("{\n" +
                "    \"contents\": [\n" +
                "      {\n" +
                "        \"parts\": [\n" +
                "          {\n" +
                "            \"text\": \"%s \"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }",prompt);
        //send request
        String response=webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/v1beta/models/gemini-2.5-flash:generateContent").build()
                        ).header("x-goog-api-key",apiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody).retrieve().bodyToMono(String.class).block();
        //extract response
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        ObjectMapper mapper=new ObjectMapper();
        try {
            JsonNode root=mapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("Generate a professional email reply for the email:");
        if(emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty()){
            prompt.append("use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("Original Email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
