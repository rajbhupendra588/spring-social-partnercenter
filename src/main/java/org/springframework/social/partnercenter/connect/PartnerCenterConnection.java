package org.springframework.social.partnercenter.connect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.core.GenericTypeResolver;
import org.springframework.social.ExpiredAuthorizationException;
import org.springframework.social.ServiceProvider;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.AbstractConnection;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.partnercenter.PartnerCenter;
import org.springframework.social.partnercenter.security.PartnerCenterServiceProvider;

public class PartnerCenterConnection extends AbstractConnection<PartnerCenter> implements RefreshableConnection{
	private static final long serialVersionUID = 4057584084077577480L;

	private transient final PartnerCenterServiceProvider serviceProvider;
	private String accessToken;
	private Long expireTime;
	private transient PartnerCenter api;
	private transient PartnerCenter apiProxy;

	/**
	 * Creates a new {@link PartnerCenterConnection} from a access grant response.
	 * Designed to be called to establish a new {@link PartnerCenterConnection} after receiving an access grant successfully.
	 * The providerUserId may be null in this case: if so, this constructor will try to resolve it using the service API obtained from
	 * the {@link PartnerCenterServiceProvider}.
	 * @param providerId the provider id e.g. "facebook".
	 * @param providerUserId the provider user id (may be null if not returned as part of the access grant)
	 * @param accessToken the granted access token
	 * @param expireTime the access token expiration time
	 * @param serviceProvider the OAuth2-based ServiceProvider
	 * @param apiAdapter the ApiAdapter for the ServiceProvider
	 */
	public PartnerCenterConnection(String providerId, String providerUserId, String accessToken, Long expireTime,
								   PartnerCenterServiceProvider serviceProvider, ApiAdapter<PartnerCenter> apiAdapter) {
		super(apiAdapter);
		this.serviceProvider = serviceProvider;
		initAccessTokens(accessToken, expireTime);
		initApi();
		initApiProxy();
		initKey(providerId, providerUserId);
	}

	/**
	 * Creates a new {@link PartnerCenterConnection} from the data provided.
	 * Designed to be called when re-constituting an existing {@link Connection} from {@link ConnectionData}.
	 * @param data the data holding the state of this connection
	 * @param serviceProvider the OAuth2-based ServiceProvider
	 * @param apiAdapter the ApiAdapter for the ServiceProvider
	 */
	public PartnerCenterConnection(ConnectionData data, PartnerCenterServiceProvider serviceProvider, ApiAdapter<PartnerCenter> apiAdapter) {
		super(data, apiAdapter);
		this.serviceProvider = serviceProvider;
		initAccessTokens(data.getAccessToken(), data.getExpireTime());
		initApi();
		initApiProxy();
	}

	// implementing Connection

	public boolean hasExpired() {
		synchronized (getMonitor()) {
			return expireTime != null && System.currentTimeMillis() - 1000 >= expireTime;
		}
	}

	public void refresh() {
		synchronized (getMonitor()) {
			AccessGrant accessGrant = serviceProvider.getAzureADAuthOperations().refreshAccess(null);
			initAccessTokens(accessGrant.getAccessToken(), accessGrant.getExpireTime());
			initApi();
		}
	}

	public PartnerCenter getApi() {
		if (apiProxy != null) {
			return apiProxy;
		} else {
			synchronized (getMonitor()) {
				return api;
			}
		}
	}

	public ConnectionData createData() {
		synchronized (getMonitor()) {
			return new ConnectionData(getKey().getProviderId(), getKey().getProviderUserId(), getDisplayName(), getProfileUrl(), getImageUrl(),
					accessToken, null, null, expireTime);
		}
	}

	// internal helpers

	private void initAccessTokens(String accessToken, Long expireTime) {
		this.accessToken = accessToken;
		this.expireTime = expireTime;
	}

	private void initApi() {
		api = serviceProvider.getApi(accessToken);
	}

	@SuppressWarnings("unchecked")
	private void initApiProxy() {
		Class<?> apiType = GenericTypeResolver.resolveTypeArgument(serviceProvider.getClass(), ServiceProvider.class);
		if (apiType.isInterface()) {
			apiProxy = (PartnerCenter) Proxy.newProxyInstance(apiType.getClassLoader(), new Class<?>[] { apiType }, new PartnerCenterConnection.ApiInvocationHandler());
		}
	}

	private class ApiInvocationHandler implements InvocationHandler {

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			synchronized (getMonitor()) {
				if (hasExpired()) {
					throw new ExpiredAuthorizationException(getKey().getProviderId());
				}
				try {
					return method.invoke(PartnerCenterConnection.this.api, args);
				} catch (InvocationTargetException e) {
					throw e.getTargetException();
				}
			}
		}
	}

	// equas() and hashCode() generated by Eclipse
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((accessToken == null) ? 0 : accessToken.hashCode());
		result = prime * result + ((expireTime == null) ? 0 : expireTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		@SuppressWarnings("rawtypes")
		PartnerCenterConnection other = (PartnerCenterConnection) obj;

		if (accessToken == null) {
			if (other.accessToken != null) return false;
		} else if (!accessToken.equals(other.accessToken)) return false;

		if (expireTime == null) {
			if (other.expireTime != null) return false;
		} else if (!expireTime.equals(other.expireTime)) return false;


		return true;
	}
}
