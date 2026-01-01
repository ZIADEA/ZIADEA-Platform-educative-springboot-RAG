package com.eduforge.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private String provider = "GEMINI";
    private final Gemini gemini = new Gemini();

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public Gemini getGemini() { return gemini; }

    public static class Gemini {
        private String apiKey = "";
        private String model = "gemini-2.5-flash";
        private int timeoutSeconds = 25;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }

        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }
}
