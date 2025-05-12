package com.github.rohit091193.codereviewhelper;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AIAnalyzer {

    private static final String OLAMMA_API_URL = "http://localhost:11434/v1/chat/completions";

    public static String analyzeCode(String javaCode) {
        try {
            String escapedCode = escapeJson(javaCode.trim());

            String jsonPayload = "{\n" +
                    "  \"model\": \"deepseek-coder\",\n" +
                    "  \"stream\": false,\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"system\",\n" +
                    "      \"content\": \"You are a strict senior Java code reviewer. For the given Java code, return a clean JSON array only in this format: [ { \\\"Code\\\": \\\"<exact faulty line>\\\", \\\"Problem\\\": \\\"<one line problem summary>\\\", \\\"Suggestion\\\": \\\"<1-2 line fix suggestion>\\\", \\\"Replacement\\\": \\\"<replacement code if applicable>\\\" } ]. No explanation, no formatting, no commentary — only the pure JSON array as response with strictly only keys Code, Problem, Suggestion & Replacement. If there are no errors Just return a non-JSON message as All Good, no issues.\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": \"Analyze the following Java code:\\n\\n" + escapedCode + "\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            HttpURLConnection conn = (HttpURLConnection) new URL(OLAMMA_API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(15000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                Scanner errScanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                String errorBody = errScanner.hasNext() ? errScanner.next() : "";
                throw new RuntimeException("API Error " + status + ": " + errorBody);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream())) {
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "No response from AI.";
                return formatForPopup(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error contacting local AI: " + e.getMessage();
        }
    }

    private static String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private static String formatForPopup(String jsonResponse) {
        int start = jsonResponse.indexOf("[");
        int end = jsonResponse.lastIndexOf("]") + 1;
        if (start == -1 || end == -1 || end <= start) return "⚠️ AI returned an invalid format.";
        return "**🔍 AI Code Review Summary:**\n\n" + jsonResponse.substring(start, end).trim();
    }
}
