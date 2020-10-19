package uk.gov.hmcts.reform.orgrolemapping;

import lombok.NoArgsConstructor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@NoArgsConstructor
//@WithTags({@WithTag("testType:Smoke")})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmptySmokeTest {
    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Value("${launchdarkly.sdk.user}")
    private String userName;

    @Value("${launchdarkly.sdk.key}")
    private String sdkKey;

    @Rule
    public FeatureFlagToggleEvaluator featureFlagToggleEvaluator = new FeatureFlagToggleEvaluator(this);

    @Tag("smoke")
    @Test
    @FeatureFlagToggle("orm-base-flag")
    public void shouldRetrieveWhenExists() {
    }

    public String getEnvironment() {
        return environment;
    }

    public String getUserName() {
        return userName;
    }

    public String getSdkKey() {
        return sdkKey;
    }
}
