package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;

import java.util.Objects;

/**
 * Convenience Hamcrest matcher for RoleAssignments.
 */
public class IsRoleAssignment extends TypeSafeMatcher<RoleAssignment> {

    private final RoleAssignment toMatch;

    public IsRoleAssignment(RoleAssignment toMatch) {
        this.toMatch = toMatch;
    }

    @Override
    protected boolean matchesSafely(RoleAssignment ra) {
        if (!ra.getRoleName().equals(toMatch.getRoleName())) {
            return false;
        } else if (!ra.getRoleCategory().equals(toMatch.getRoleCategory())) {
            return false;
        } else if (!ra.getRoleType().equals(toMatch.getRoleType())) {
            return false;
        } else if (!ra.getGrantType().equals(toMatch.getGrantType())) {
            return false;
        } else if (!ra.getClassification().equals(toMatch.getClassification())) {
            return false;
        } else if (ra.isReadOnly() != toMatch.isReadOnly()) {
            return false;
        } else if (!ra.getActorIdType().equals(toMatch.getActorIdType())) {
            return false;
        } else if (ra.getAttributes().size() != toMatch.getAttributes().size()) {
            return false;
        } else if (!ra.getAttributes().equals(toMatch.getAttributes())) {
            return false;
        } else if ((ra.getAuthorisations() == null && toMatch.getAuthorisations() != null)
                || (ra.getAuthorisations() != null && toMatch.getAuthorisations() == null)
                || !Objects.equals(ra.getAuthorisations(), toMatch.getAuthorisations())) {
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matches " + this.toMatch);
    }

    public static Matcher<RoleAssignment> isRoleAssignment(RoleAssignment toMatch) {
        return new IsRoleAssignment(toMatch);
    }
}
