package uk.gov.hmcts.reform.orgrolemapping.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

class AssignmentRequestBuilderTest {

    @Test
    void buildAssignmentRequest() {
        assertNotNull(AssignmentRequestBuilder.buildAssignmentRequest(false));
    }

    @Test
    void buildRequest() {
        assertNotNull(AssignmentRequestBuilder.buildRequest(false));
    }

    @Test
    void buildRequestedRoleCollection() {
        assertTrue(AssignmentRequestBuilder.buildRequestedRoleCollection().size() >= 1);
    }

    @Test
    void buildRoleAssignment() {
        assertNotNull(AssignmentRequestBuilder.buildRoleAssignment());
        assertNotNull(AssignmentRequestBuilder.buildRoleAssignment().getAttributes());
    }

    @Test
    void buildAttributesFromFile() {
        assertNotNull(AssignmentRequestBuilder.buildAttributesFromFile("attributes.json"));
    }

    @Test
    void buildRequestedRoleForStaff() {
        assertNotNull(AssignmentRequestBuilder.buildRequestedRoleForStaff());
    }

    @Test
    void convertUserProfileToUserAccessProfile() {
        Set<UserAccessProfile> userAccessProfiles = AssignmentRequestBuilder
                .convertUserProfileToUserAccessProfile(TestDataBuilder
                        .buildUserProfile("21334a2b-79ce-44eb-9168-2d49a744be9c"));
        userAccessProfiles.forEach(role -> {
                assertNotNull(role.getId());
                assertNotNull(role.getAreaOfWorkId());
                assertNotNull(role.getPrimaryLocationId());
                assertNotNull(role.getPrimaryLocationName());
                assertNotNull(role.getRoleId());
                assertNotNull(role.getRoleName());
                assertNotNull(role.getServiceCode());
            }
        );
        assertEquals(1, userAccessProfiles.size());
    }
}