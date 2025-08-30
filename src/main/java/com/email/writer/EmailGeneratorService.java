package com.email.writer;

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
        //extract response
        return null;
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
