package uk.gov.hmcts.reform.orgrolemapping.domain.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JavaType;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.Request;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserAccessProfile;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.stubs.StubRoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.util.JacksonUtils;

@Service
@Slf4j
public class RequestMappingService {

	@Autowired
    private StubRoleAssignmentService roleAssignmentService;
    private StatelessKieSession kieSession;

    public RequestMappingService(StubRoleAssignmentService roleAssignmentService, StatelessKieSession kieSession) {
        this.roleAssignmentService = roleAssignmentService;
        this.kieSession = kieSession;
    }


    /**
     * For each caseworker represented in the map, determine what the role assignments should be,
     * and update them in the role assignment service.
     */
    public List<RoleAssignment> createCaseWorkerAssignments(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {
    	// Get the role assignments for each caseworker in the input profiles.
    	Map<String, Collection<RoleAssignment>> usersRoleAssignments = calculateCaseworkerRoleAssignments(usersAccessProfiles);
    	// The response body is a list of ....???....
    	List<RoleAssignment> responseBody = updateCaseworkersRoleAssignments(usersRoleAssignments);
        return responseBody;
    }

    /**
     * Apply the role assignment mapping rules to determine what the role assignments should be
     * for each caseworker represented in the map.
     */
    private Map<String, Collection<RoleAssignment>> calculateCaseworkerRoleAssignments(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {
		// Create a map to hold the role assignments for each user.
		Map<String, Collection<RoleAssignment>> usersRoleAssignments = new HashMap<>();
		// Make sure every user in the input collection has a list in the map.  This includes users
		// who have been deleted, for whom no role assignments will be created by the rules.
		usersAccessProfiles.keySet().forEach(k -> usersRoleAssignments.put(k, new ArrayList<>()));
		// Get all the role assignments the rules create for the set of access profiles.
		List<RoleAssignment> roleAssignments =  mapUserAccessProfiles(usersAccessProfiles);
		// Add each role assignment to the results map. 
		roleAssignments.forEach(ra -> usersRoleAssignments.get(ra.getActorId()).add(ra));
		return usersRoleAssignments;
    }

    /**
     * Run the mapping rules to generate all the role assignments each caseworker represented in the map.
     */
    private List<RoleAssignment> mapUserAccessProfiles(Map<String, Set<UserAccessProfile>> usersAccessProfiles) {
        final String ROLE_ASSIGNMENTS_QUERY_NAME = "getRoleAssignments";
        final String ROLE_ASSIGNMENTS_RESULTS_KEY = "roleAssignments";

    	// Combine all the user profiles into a single collection for the rules engine.
    	Set<UserAccessProfile> allProfiles = new HashSet<>();
    	usersAccessProfiles.forEach((k, v) -> allProfiles.addAll(v));

    	// Sequence of processing for executing the rules:
    	//   1. add all the profiles
    	//   2. fire all the rules
    	//   3. retrieve all the created role assignments
    	//      (into a variable populated by the results of a query defined in the rules).
    	List<Command<?>> commands = new ArrayList<>();
        commands.add(CommandFactory.newInsertElements(allProfiles));
        commands.add(CommandFactory.newFireAllRules());
        commands.add(CommandFactory.newQuery(ROLE_ASSIGNMENTS_RESULTS_KEY, ROLE_ASSIGNMENTS_QUERY_NAME));

        // Run the rules
        ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(commands));

        // Extract all created role assignments using the query defined in the rules.
        List<RoleAssignment> roleAssignments = new ArrayList<>();
        QueryResults queryResults = (QueryResults)results.getValue(ROLE_ASSIGNMENTS_RESULTS_KEY);
        for (QueryResultsRow row : queryResults) {
        	roleAssignments.add((RoleAssignment)row.get("$roleAssignment"));
        }
        return roleAssignments;
    }

    /**
     * Update the role assignments for every caseworker represented in the map.
     * Note that some caseworker IDs may have empty role assignment collections.
     * This is OK - these caseworkers have been deleted (or just don't have any appointments which map to roles). 
     */
	List<RoleAssignment> updateCaseworkersRoleAssignments(Map<String, Collection<RoleAssignment>> usersRoleAssignments) {
		return
				usersRoleAssignments.entrySet().stream()
				.flatMap(entry -> updateCaseworkerRoleAssignments(entry.getKey(), entry.getValue()).stream())
				.collect(Collectors.toList());
	}

	/**
	 * Update a single caseworker's role assignments, using the staff organisational role mapping process ID
	 * and the user's ID as the process and reference values.
	 */
	Collection<RoleAssignment> updateCaseworkerRoleAssignments(String userId, Collection<RoleAssignment> roleAssignments) {
		String process = "staff-organisational-role-mapping";
		String reference = userId;
		return updateRoleAssignments(process, reference, roleAssignments);
	}

	/**
	 * Send an update of role assignments to the role assignment service for a process/reference pair.
	 */
	Collection<RoleAssignment> updateRoleAssignments(String process, String reference, Collection<RoleAssignment> roleAssignments) {
		AssignmentRequest assignmentRequest =
				AssignmentRequest.builder()
				.request(
						Request.builder()
						.requestType(RequestType.CREATE)
						.replaceExisting(true)
						.process(process)
						.reference(reference)
						.assignerId("")
						.authenticatedUserId("")
						.clientId("")
						.correlationId("")
						.build())
				.requestedRoles(roleAssignments)
				.build();
        return roleAssignmentService.createRoleAssignment(assignmentRequest);		
	}



	
	
	// ----------------------------------------------------- TEST CODE -------------------------------------------------------

    public static void main(String[] args) throws Exception {
    	RequestMappingService m = new RequestMappingService(new StubRoleAssignmentService(), kieSession());
    	for (Map<String, Set<UserAccessProfile>> usersAccessProfiles : loadScenarios()) {
	    	Collection<RoleAssignment> roleAssignments = m.createCaseWorkerAssignments(usersAccessProfiles);
	    	System.out.println("******************************************************************************************");
	    	System.out.println(roleAssignments);
	    	System.out.println("******************************************************************************************");
	    	m.roleAssignmentService.getRoleAssignmentsByProcessAndReference()
	    	.forEach((k, v) -> {
	    		System.out.println(k + " : " + v);
	    	});
	    	System.out.println("******************************************************************************************");
    	}
    }

    public static StatelessKieSession kieSession() {
        return KieServices.Factory.get().getKieClasspathContainer().newStatelessKieSession("org-role-mapping-validation-session");
    }

    /**
	 * Load user profiles from the user-access-profiles.json resource.
	 */
	private static List<Map<String, Set<UserAccessProfile>>> loadScenarios() {
		try {
			try (InputStream input = ValidationModelService.class.getResourceAsStream("user-access-profiles.json")) {
				JavaType inner = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(Set.class, UserAccessProfile.class);
				JavaType outer = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(List.class, inner);
				List<Set<UserAccessProfile>> userProfiles = JacksonUtils.MAPPER.readValue(input, outer);
				List<Map<String, Set<UserAccessProfile>>> scenarios = new ArrayList<>();
				for (Set<UserAccessProfile> scenarioProfiles : userProfiles) {
					Map<String, Set<UserAccessProfile>> scenario = new HashMap<>();
					scenarioProfiles.forEach(p -> {
						Set<UserAccessProfile> thisUsersProfiles = scenario.get(p.getId());
						if (thisUsersProfiles == null) {
							thisUsersProfiles = new HashSet<>();
							scenario.put(p.getId(), thisUsersProfiles);
						}
						thisUsersProfiles.add(p);
					});
					scenarios.add(scenario);
				}
				return scenarios;
			}
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load user access profiles.", t);
		}
	}
}
