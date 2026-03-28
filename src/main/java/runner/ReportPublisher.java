package runner;

public class ReportPublisher {

    public static void main(String[] args) throws Exception {
        String cucumberJsonPath = args.length > 0
                ? args[0]
                : "build/karate-reports/features.users.json";

        String confluenceBaseUrl = System.getenv("CONFLUENCE_BASE_URL");
        String confluenceEmail   = System.getenv("CONFLUENCE_EMAIL");
        String confluenceToken   = System.getenv("CONFLUENCE_API_TOKEN");
        String confluencePageId  = System.getenv("CONFLUENCE_PAGE_ID");

        if (confluenceBaseUrl == null || confluenceEmail == null
                || confluenceToken == null || confluencePageId == null) {
            System.out.println("Missing one or more environment variables:");
            System.out.println("  CONFLUENCE_BASE_URL, CONFLUENCE_EMAIL, CONFLUENCE_API_TOKEN, CONFLUENCE_PAGE_ID");
            System.exit(1);
        }

        System.out.println("Parsing report: " + cucumberJsonPath);
        ReportParser parser = new ReportParser();
        parser.parse(cucumberJsonPath);

        System.out.println("Total   : " + parser.getTotalScenarios());
        System.out.println("Passed  : " + parser.getPassedScenarios() + " (" + parser.getPassPercentage() + "%)");
        System.out.println("Failed  : " + parser.getFailedScenarios() + " (" + parser.getFailPercentage() + "%)");

        if (!parser.getFailedTests().isEmpty()) {
            System.out.println("Failed tests:");
            parser.getFailedTests().forEach(t -> System.out.println("  - " + t));
        }

        System.out.println("Uploading to Confluence...");
        ConfluenceUploader uploader = new ConfluenceUploader(
                confluenceBaseUrl,
                confluenceEmail,
                confluenceToken,
                confluencePageId
        );
        uploader.uploadReport(parser);
    }
}