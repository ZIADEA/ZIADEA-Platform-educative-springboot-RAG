package com.eduforge.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eduforge")
public class AppProperties {

    private final Storage storage = new Storage();
    private final Rag rag = new Rag();
    private final Ai ai = new Ai();

    public Storage getStorage() { return storage; }
    public Rag getRag() { return rag; }
    public Ai getAi() { return ai; }

    public static class Storage {
        /**
         * Dossier local où stocker les uploads (PDF/PPTX) + fichiers dérivés.
         * Ex: ./data/uploads
         */
        private String uploadDir = "./data/uploads";

        public String getUploadDir() { return uploadDir; }
        public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    }

    public static class Rag {
        private int topK = 6;
        private int chunkChars = 1000;

        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }

        public int getChunkChars() { return chunkChars; }
        public void setChunkChars(int chunkChars) { this.chunkChars = chunkChars; }
    }

    public static class Ai {
        /**
         * GEMINI_WITH_FALLBACK (par défaut), GEMINI_ONLY, MOCK_ONLY
         */
        private String mode = "GEMINI_WITH_FALLBACK";
        private String geminiApiKey = "";
        private String geminiModel = "gemini-2.0-flash";

        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }

        public String getGeminiApiKey() { return geminiApiKey; }
        public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }

        public String getGeminiModel() { return geminiModel; }
        public void setGeminiModel(String geminiModel) { this.geminiModel = geminiModel; }
    }
}
