package com.eduforge.platform.service.ai;

import org.springframework.stereotype.Service;

@Service
public class MockAiGateway implements AiGateway {

    @Override
    public String generateJson(String systemInstruction, String userPrompt) {
        // Le mock ne lit pas le systemInstruction : il fabrique du JSON stable.
        // L’agent, lui, passe déjà le CONTEXTE RAG dans userPrompt => on s’en sert.
        String ctx = userPrompt;
        String seed = ctx.length() > 200 ? ctx.substring(0, 200) : ctx;
        String q1 = "D’après le contenu, quelle affirmation est correcte ?";
        String a = "Elle reprend fidèlement le passage fourni.";
        String b = "Elle utilise des connaissances externes non présentes.";
        String c = "Elle contredit le passage fourni.";
        String d = "Elle parle d’un sujet absent du cours.";
        String json = """
        {
          "questions": [
            {
              "question": "%s",
              "choices": { "A": "%s", "B": "%s", "C": "%s", "D": "%s" },
              "correct": "A",
              "explanation": "La réponse A est la seule cohérente avec le contexte RAG fourni."
            }
          ],
          "meta": { "provider": "MOCK", "note": %s }
        }
        """.formatted(escape(q1), escape(a), escape(b), escape(c), escape(d), quote(seed));
        return json;
    }

    @Override
    public String name() { return "MOCK"; }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private String quote(String s) {
        return "\"" + escape(s) + "\"";
    }
}
