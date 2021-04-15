package uk.gov.hmcts.reform.orgrolemapping.launchdarkly;


import com.launchdarkly.sdk.LDUser;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.orgrolemapping.launchdarkly.FeatureConditionEvaluator.SERVICE_NAME;

@Component
public class LDFlagRegister implements CommandLineRunner {

    @Autowired
    FlagEventListener flagEventListener;

    @Autowired
    FeatureConditionEvaluator featureConditionEvaluator;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;

    @Getter
    public static final Map<String,Boolean> droolFlagStates = new HashMap<>();

        @Override
        public void run(String... args) throws Exception {
            LDUser user = new LDUser.Builder(environment)
                    .firstName("orm")
                    .lastName("am")
                    .custom(SERVICE_NAME, "am_org_role_mapping_service")
                    .build();
        //This if config file flow
        for (FlagConfig flag : FlagConfigs.getFlagConfigs().getValues()) {

            flagEventListener.logWheneverOneFlagChangesForOneUser(flag.getName(), user);
            // FlagRefreshService.initFlagConfig(flag.getName())
            //1) This is first time flag value being read and inserted in the DB, hence no need to check orm-refresh-role flag.
            // First check if the flag already exist in the table, if yes then skip. If No then insert with default config value.
            // If inserting, then retrieve the exclusive lock on the table, so that other nodes cannot insert the same entry.
            // insert the flag
            // 2) If DB operation executed successfully, then add the flag with default status in the static map as well.
            droolFlagStates.put(flag.getName(),flag.getDefaultValue()); //add this inthe above method
        }
    }
}

