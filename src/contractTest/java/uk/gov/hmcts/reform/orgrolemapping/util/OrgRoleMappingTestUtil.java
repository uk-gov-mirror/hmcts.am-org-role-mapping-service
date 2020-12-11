package uk.gov.hmcts.reform.orgrolemapping.util;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;

import java.util.Map;

public class OrgRoleMappingTestUtil {

    private OrgRoleMappingTestUtil() {
    }

    public static HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    @NotNull
    public static Map<String, String> getResponseHeaders(String type) {
        Map<String, String> responseHeaders = Maps.newHashMap();
        switch (type) {
            case "getRole":
                responseHeaders.put("Content-Type",
                        "application/vnd.uk.gov.hmcts.role-assignment-service.get-roles" +
                                "+json;charset=UTF-8;version=1.0");
                break;

            case "getAssignment":
                responseHeaders.put("Content-Type",
                        "application/vnd.uk.gov.hmcts.role-assignment-service.get-assignments" +
                                "+json;charset=UTF-8;version=1.0");
                break;
            case "create":
                responseHeaders.put("Content-Type", "application/vnd.uk.gov.hmcts.role-assignment-service."
                        + "create-assignments+json");
                break;
        }
        return responseHeaders;
    }


}
