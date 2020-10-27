package uk.gov.hmcts.reform.orgrolemapping.domain.service.stubs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
/**
 * Simple stub of role assignment functionality which keeps a repository of role
 * assignments keyed on process and reference and allows updates with or without
 * replaceExisting.
 */
public class StubRoleAssignmentService {

	@Getter
	private final Map<String, Set<RoleAssignment>> roleAssignmentsByProcessAndReference = new HashMap<>();

    public Collection<RoleAssignment> createRoleAssignment(AssignmentRequest assignmentRequest) {
    	String process = assignmentRequest.getRequest().getProcess();
    	String reference = assignmentRequest.getRequest().getReference();
    	String key = process + "/" + reference;

    	// Make sure we have a set of role assignments in the repository for this key.
    	Set<RoleAssignment> roleAssignments = roleAssignmentsByProcessAndReference.get(key);
    	if (roleAssignments == null) {
    		roleAssignments = new HashSet<>();
    		roleAssignmentsByProcessAndReference.put(key, roleAssignments);
    	}

    	// Delete existing role assignments?
    	if (assignmentRequest.getRequest().isReplaceExisting()) {
    		roleAssignments.clear();
    	}

    	// Add the new ones and return them.
    	roleAssignments.addAll(assignmentRequest.getRequestedRoles());
    	return roleAssignments;
    }

}
