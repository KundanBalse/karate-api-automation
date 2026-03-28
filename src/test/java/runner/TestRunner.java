package runner;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Test;

class TestRunner {

    @Test
    void testAll() {
        Karate.run("classpath:features")
                .relativeTo(getClass())
                .outputCucumberJson(true)
                .outputHtmlReport(true)
                .parallel(5);
    }
}
