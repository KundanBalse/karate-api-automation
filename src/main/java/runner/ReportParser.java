package runner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ReportParser {

    private int totalScenarios = 0;
    private int passedScenarios = 0;
    private int failedScenarios = 0;
    private List<String> failedTests = new ArrayList<>();
    private List<String> errorMessages = new ArrayList<>();

    public void parse(String cucumberJsonPath) throws Exception {
        JSONParser parser = new JSONParser();
        Object parsed = parser.parse(new FileReader(cucumberJsonPath));

        JSONArray features;
        if (parsed instanceof JSONArray) {
            features = (JSONArray) parsed;
        } else {
            features = new JSONArray();
            features.add(parsed);
        }

        for (Object featureObj : features) {
            JSONObject feature = (JSONObject) featureObj;
            String featureName = (String) feature.get("name");
            JSONArray elements = (JSONArray) feature.get("elements");

            if (elements == null) continue;

            for (Object elementObj : elements) {
                JSONObject scenario = (JSONObject) elementObj;
                String scenarioName = (String) scenario.get("name");
                JSONArray steps = (JSONArray) scenario.get("steps");

                boolean scenarioPassed = true;
                StringBuilder errorDetail = new StringBuilder();

                if (steps != null) {
                    for (Object stepObj : steps) {
                        JSONObject step = (JSONObject) stepObj;
                        JSONObject result = (JSONObject) step.get("result");
                        if (result == null) continue;

                        String status = (String) result.get("status");
                        if (!"passed".equals(status)) {
                            scenarioPassed = false;
                            String errorMessage = (String) result.get("error_message");
                            if (errorMessage != null) {
                                errorDetail.append(errorMessage, 0,
                                        Math.min(errorMessage.length(), 200));
                            }
                        }
                    }
                }

                totalScenarios++;
                if (scenarioPassed) {
                    passedScenarios++;
                } else {
                    failedScenarios++;
                    failedTests.add(featureName + " > " + scenarioName);
                    if (errorDetail.length() > 0) {
                        errorMessages.add(errorDetail.toString());
                    }
                }
            }
        }
    }

    public double getPassPercentage() {
        return totalScenarios == 0 ? 0 : Math.round((passedScenarios * 100.0 / totalScenarios) * 100.0) / 100.0;
    }

    public double getFailPercentage() {
        return totalScenarios == 0 ? 0 : Math.round((failedScenarios * 100.0 / totalScenarios) * 100.0) / 100.0;
    }

    public int getTotalScenarios() { return totalScenarios; }
    public int getPassedScenarios() { return passedScenarios; }
    public int getFailedScenarios() { return failedScenarios; }
    public List<String> getFailedTests() { return failedTests; }
    public List<String> getErrorMessages() { return errorMessages; }
}