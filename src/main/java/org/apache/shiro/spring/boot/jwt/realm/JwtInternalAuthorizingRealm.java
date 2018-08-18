package org.apache.shiro.spring.boot.jwt.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.biz.authc.DelegateAuthenticationInfo;
import org.apache.shiro.biz.authc.token.DelegateAuthenticationToken;
import org.apache.shiro.biz.realm.InternalAuthorizingRealm;
import org.apache.shiro.spring.boot.jwt.JwtPrincipalRepository;
import org.apache.shiro.spring.boot.jwt.token.StatelessToken;
import org.apache.shiro.util.ByteSource;

public class JwtInternalAuthorizingRealm extends InternalAuthorizingRealm {

	@Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof StatelessToken;
    }
	
	@Override
	protected DelegateAuthenticationToken createDelegateAuthenticationToken(AuthenticationToken token) {
		return (StatelessToken) token;
	}

	@Override
	protected AuthenticationInfo doGetInternalAuthenticationInfo(AuthenticationToken token) {

		SimpleAccount account = null;
		if (getRepository() instanceof JwtPrincipalRepository) {

			JwtPrincipalRepository jwtRepository = (JwtPrincipalRepository) getRepository();
			StatelessToken upToken = (StatelessToken) token;

			// do real thing
			// new delegate authentication token and invoke doAuthc method
			DelegateAuthenticationInfo delegateAuthcInfo = getRepository()
					.getAuthenticationInfo(this.createDelegateAuthenticationToken(token));
			if (delegateAuthcInfo != null && jwtRepository.validateToken(upToken.getToken())) {
				account = new SimpleAccount(delegateAuthcInfo.getPrincipal(), delegateAuthcInfo.getCredentials(),
						ByteSource.Util.bytes(delegateAuthcInfo.getCredentialsSalt()), getName());
			}

		}
		return account;

	}

}
