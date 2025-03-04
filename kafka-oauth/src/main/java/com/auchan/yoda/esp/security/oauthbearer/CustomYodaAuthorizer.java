package com.auchan.yoda.esp.security.oauthbearer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.apache.kafka.common.Endpoint;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.server.authorizer.AclCreateResult;
import org.apache.kafka.server.authorizer.AclDeleteResult;
import org.apache.kafka.server.authorizer.Action;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;
import org.apache.kafka.server.authorizer.AuthorizationResult;
import org.apache.kafka.server.authorizer.Authorizer;
import org.apache.kafka.server.authorizer.AuthorizerServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomYodaAuthorizer implements Authorizer {

	private static final Logger log = LoggerFactory.getLogger(CustomYodaAuthorizer.class);

	@Override
	public Map<Endpoint, ? extends CompletionStage<Void>> start(AuthorizerServerInfo serverInfo) {
		return serverInfo.endpoints().stream().collect(Collectors.toMap(endpoint -> endpoint,
				endpoint -> CompletableFuture.completedFuture(null), (a, b) -> b));
	}

	/**
	 * Check scopes from JWT to validate topic/resource operations.
	 *
	 * @param requestContext request data
	 * @param actions        List of Acl actions
	 * @return List of authorization results for each action in the same order as
	 *         the provided actions : List<AuthorizationResult>
	 */
	@Override
	public List<AuthorizationResult> authorize(AuthorizableRequestContext requestContext, List<Action> actions) {
		List<AuthorizationResult> authorizationResults = new ArrayList<>();
		try {
			log.info("Starting Authorization.");
			if (!(requestContext.principal() instanceof CustomPrincipal)) {
				log.error("Session Principal is not using the proper class. Should be instance of CustomPrincipal.");
				return denyAll(actions);
			}

			CustomPrincipal principal = (CustomPrincipal) requestContext.principal();
			if (principal.getOauthBearerTokenJwt() == null) {
				log.error("Custom Principal does not contain token information.");
				return denyAll(actions);
			}

			OAuthBearerTokenJwt jwt = principal.getOauthBearerTokenJwt();
			if (jwt.scope() == null || jwt.scope().isEmpty()) {
				log.error("No scopes provided in JWT. Unable to Authorize.");
				return denyAll(actions);
			}

			for (Action action : actions) {
				log.info("Operation request Info: {}", action.operation().toString());
				log.info("Resource request Info: {}", action.resourcePattern().toString());
				java.util.Set<String> scopes = jwt.scope();
				List<OAuthScope> scopeInfo = parseScopes(scopes);
				String operationStr = action.operation().toString();
				authorizationResults.add(checkAuthorization(scopeInfo, action.resourcePattern(), operationStr));
				return authorizationResults;
			}
		} catch (Exception e) {
			log.error("Error in authorization. ", e);
		}

		return denyAll(actions);
	}

	private static List<AuthorizationResult> denyAll(List<Action> actions) {
		return actions.stream().map(a -> AuthorizationResult.DENIED).collect(Collectors.toList());
	}

	/**
	 * Check authorization against scopes.
	 *
	 * @param scopeInfo list of scopes
	 * @param resource  resource info
	 * @param operation operation performed
	 * @return true /false
	 */
	protected AuthorizationResult checkAuthorization(List<OAuthScope> scopeInfo, ResourcePattern resource,
			String operation) {
		for (OAuthScope scope : scopeInfo) {
			String lowerCaseOperation = operation.toLowerCase();
			String lowerCaseResourceName = resource.name().toLowerCase();
			String lowerCaseCaseResourceType = resource.resourceType().toString().toLowerCase();

			boolean operationVal = scope.getOperation().toLowerCase().equals(lowerCaseOperation);
			boolean nameVal = scope.getResourceName().toLowerCase().equals(lowerCaseResourceName);
			boolean typeVal = scope.getResourceType().toLowerCase().equals(lowerCaseCaseResourceType);

			if (operationVal && nameVal && typeVal) {
				log.info("Successfully Authorized.");
				return AuthorizationResult.ALLOWED;
			}
		}
		log.info("Not Authorized to operate on the given resource.");
		return AuthorizationResult.DENIED;
	}

	/**
	 * Parse topic and Operation out of scope.
	 *
	 * @param scopes set of scopes
	 * @return return list of pairs, each pair is a topic/operation
	 *         <p>
	 *         Scope format urn:kafka:<resourceType>:<resourceName>:<operation>
	 */
	protected List<OAuthScope> parseScopes(java.util.Set<String> scopes) {
		List<OAuthScope> result = new ArrayList<>();
		for (String scope : scopes) {
			String[] scopeArray = scope.split("\\s+");
			for (String str : scopeArray) {
				convertScope(result, str);
			}
		}
		return result;
	}

	/**
	 * convertScope.
	 * 
	 * @param result list of scopesInfo
	 * @param scope  string of scope
	 */
	private void convertScope(List<OAuthScope> result, String scope) {
		String[] str = scope.split(":");
		if (str.length == 5) {
			String type = str[2];
			String name = str[3];
			String operation = str[4];
			OAuthScope oAuthScope = new OAuthScope();
			oAuthScope.setOperation(operation);
			oAuthScope.setResourceName(name);
			oAuthScope.setResourceType(type);
			result.add(oAuthScope);
		} else {
			log.error("Unable to parse scope. Incorrect format: {}.", scope);
		}
	}

	@Override
	public List<? extends CompletionStage<AclCreateResult>> createAcls(AuthorizableRequestContext requestContext,
			List<AclBinding> aclBindings) {
		log.error("createAcls is not supported by Yoda for Kafka");

		return aclBindings.stream().map(ab -> {
			CompletableFuture<AclCreateResult> completableFuture = new CompletableFuture<>();
			completableFuture.completeExceptionally(
					new UnsupportedOperationException("createAcls is not supported by Yoda for Kafka"));
			return completableFuture;
		}).collect(Collectors.toList());
	}

	@Override
	public List<? extends CompletionStage<AclDeleteResult>> deleteAcls(AuthorizableRequestContext requestContext,
			List<AclBindingFilter> aclBindingFilters) {
		log.error("deleteAcls is not supported by Yoda for Kafka");
		return aclBindingFilters.stream().map(ab -> {
			CompletableFuture<AclDeleteResult> completableFuture = new CompletableFuture<>();
			completableFuture.completeExceptionally(
					new UnsupportedOperationException("deleteAcls is not supported by Yoda for Kafka"));
			return completableFuture;
		}).collect(Collectors.toList());
	}

	@Override
	public Iterable<AclBinding> acls(AclBindingFilter filter) {
		log.error("(getting) acls is not supported by Yoda for Kafka");
		throw new UnsupportedOperationException("(getting) acls is not supported by Yoda for Kafka");
	}

	@Override
	public void close() {
		log.info("close() called on authorizer.");
	}

	@Override
	public void configure(Map<String, ?> configs) {
		log.info("configure() called on authorizer with config {}", configs);
	}
}
