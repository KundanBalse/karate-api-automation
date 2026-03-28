package runner;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;

public class ConfluenceUploader {

    private final String baseUrl;
    private final String authToken;
    private final String pageId;

    public ConfluenceUploader(String baseUrl, String email, String apiToken, String pageId) {
        this.baseUrl = baseUrl;
        this.authToken = Base64.getEncoder().encodeToString((email + ":" + apiToken).getBytes());
        this.pageId = pageId;
    }

    public void uploadReport(ReportParser report) throws Exception {
        int currentVersion = getCurrentVersion();
        String body = buildConfluenceBody(report);
        updatePage(currentVersion + 1, body);
    }

    private int getCurrentVersion() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/wiki/rest/api/content/" + pageId))
                .header("Authorization", "Basic " + authToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        int versionIndex = body.indexOf("\"number\":");
        if (versionIndex == -1) return 1;
        int start = versionIndex + 9;
        int end = body.indexOf(",", start);
        return Integer.parseInt(body.substring(start, end).trim());
    }

    private void updatePage(int newVersion, String htmlBody) throws Exception {
        String payload = "{"
                + "\"version\":{\"number\":" + newVersion + "},"
                + "\"title\":\"API Automation Report\","
                + "\"type\":\"page\","
                + "\"body\":{"
                + "\"storage\":{"
                + "\"value\":\"" + htmlBody.replace("\"", "\\\"") + "\","
                + "\"representation\":\"storage\""
                + "}"
                + "}"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/wiki/rest/api/content/" + pageId))
                .header("Authorization", "Basic " + authToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("Confluence page updated successfully.");
        } else {
            System.out.println("Failed to update Confluence page. Status: " + response.statusCode());
            System.out.println("Response: " + response.body());
        }
    }

    private String buildConfluenceBody(ReportParser report) {
        StringBuilder sb = new StringBuilder();

        sb.append("<h2>API Automation Test Report</h2>");
        sb.append("<p><strong>Total Scenarios:</strong> ").append(report.getTotalScenarios()).append("</p>");
        sb.append("<p><strong>Passed:</strong> ").append(report.getPassedScenarios())
                .append(" (").append(report.getPassPercentage()).append("%)</p>");
        sb.append("<p><strong>Failed:</strong> ").append(report.getFailedScenarios())
                .append(" (").append(report.getFailPercentage()).append("%)</p>");

        sb.append("<h3>Summary Table</h3>");
        sb.append("<table><tbody>");
        sb.append("<tr><th>Metric</th><th>Value</th></tr>");
        sb.append("<tr><td>Total</td><td>").append(report.getTotalScenarios()).append("</td></tr>");
        sb.append("<tr><td>Passed</td><td>").append(report.getPassedScenarios()).append("</td></tr>");
        sb.append("<tr><td>Failed</td><td>").append(report.getFailedScenarios()).append("</td></tr>");
        sb.append("<tr><td>Pass %</td><td>").append(report.getPassPercentage()).append("%</td></tr>");
        sb.append("<tr><td>Fail %</td><td>").append(report.getFailPercentage()).append("%</td></tr>");
        sb.append("</tbody></table>");

        List<String> failedTests = report.getFailedTests();
        if (!failedTests.isEmpty()) {
            sb.append("<h3>Failed Scenarios</h3><ul>");
            for (String test : failedTests) {
                sb.append("<li>").append(test).append("</li>");
            }
            sb.append("</ul>");

            sb.append("<h3>Error Details</h3><ul>");
            for (String error : report.getErrorMessages()) {
                sb.append("<li><code>").append(error).append("</code></li>");
            }
            sb.append("</ul>");
        } else {
            sb.append("<p><strong>All scenarios passed.</strong></p>");
        }

        return sb.toString();
    }
}