/*
 * Copyright (c) 2018, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.shiro.spring.boot.utils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.biz.utils.StringUtils;
import org.apache.shiro.spring.boot.jwt.JwtPayload;
import org.apache.shiro.util.CollectionUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 基于JJwt组件的jwt工具对象
 * 
 * @author ： <a href="https://github.com/vindell">vindell</a>
 */
public class JJwtUtils {

	public static final String ROLE_REFRESH_TOKEN = "ROLE_REFRESH_TOKEN";
	public static final String CLAIM_KEY_USER_ID = "user_id";
	public static final String CLAIM_KEY_AUTHORITIES = "scope";
	public static final String CLAIM_KEY_ACCOUNT_ENABLED = "enabled";
	public static final String CLAIM_KEY_ACCOUNT_NON_LOCKED = "non_locked";
	public static final String CLAIM_KEY_ACCOUNT_NON_EXPIRED = "non_expired";

	public static JwtBuilder jwtBuilder(String jwtId, String subject, String issuer, String audience, Map<String, Object> claims,
			long period) {

		// 当前时间戳
		long currentTimeMillis = System.currentTimeMillis();
		JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT");
		// 声明信息：调用setClaims需要在其他设置之前，不然会丢失数据
		if(!CollectionUtils.isEmpty(claims)) {
			builder.setClaims(claims);
		}
		// Jwt主键ID
		if (StringUtils.hasText(jwtId)) {
			builder.setId(jwtId);
			builder.claim(Claims.ID, jwtId);
		}
		// 用户名主题
		builder.setSubject(subject);
		builder.claim(Claims.SUBJECT, subject);
		// 接收对象
		if (StringUtils.hasText(audience)) {
			builder.setAudience(audience);
			builder.claim(Claims.AUDIENCE, audience);
		}
		// 签发者
		if (StringUtils.hasText(issuer)) {
			builder.setIssuer(issuer);
			builder.claim(Claims.ISSUER, issuer);
		}
		// 签发时间
		Date now = new Date(currentTimeMillis);
		builder.setIssuedAt(now);
		// Token过期时间
		if (period >= 0) {
			// 有效时间
			Date expiration = new Date(currentTimeMillis + period * 1000);
			builder.setExpiration(expiration).setNotBefore(now);
		}
		return builder;
	}

	public static JwtBuilder jwtBuilder(String jwtId, String subject,
			String issuer, String audience, String roles, String permissions, long period) {
		
		// 当前时间戳
		long currentTimeMillis = System.currentTimeMillis();
		JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT");
		
		// Jwt主键ID
		if (StringUtils.hasText(jwtId)) {
			builder.setId(jwtId);
			builder.claim(Claims.ID, jwtId);
		}
		// 用户名主题
		builder.setSubject(subject);
		builder.claim(Claims.SUBJECT, subject);
		// 接收对象
		if (StringUtils.hasText(audience)) {
			builder.setAudience(audience);
			builder.claim(Claims.AUDIENCE, audience);
		}
		// 签发者
		if (StringUtils.hasText(issuer)) {
			builder.setIssuer(issuer);
			builder.claim(Claims.ISSUER, issuer);
		}
		// 签发时间
		Date now = new Date(currentTimeMillis);
		builder.setIssuedAt(now);
		// Token过期时间
		if (period >= 0) {
			// 有效时间
			Date expiration = new Date(currentTimeMillis + period * 1000);
			builder.setExpiration(expiration).setNotBefore(now);
		}
		// 角色
		if (StringUtils.hasText(roles)) {
			builder.claim("roles", roles);
		}
		// 权限
		if (StringUtils.hasText(permissions)) {
			builder.claim("perms", permissions);
		}
		return builder;
	}

	public static JwtPayload payload(Claims claims) throws ParseException {

		JwtPayload payload = new JwtPayload();
		
		payload.setTokenId(claims.getId());
		payload.setClientId(claims.getSubject());// 用户名
		payload.setIssuer(claims.getIssuer());// 签发者
		payload.setIssuedAt(claims.getIssuedAt());// 签发时间
		payload.setExpiration(claims.getExpiration()); // 过期时间
		payload.setNotBefore(claims.getNotBefore());
		
		payload.setAudience(Arrays.asList(claims.getAudience()));// 接收方
		payload.setClaims(claims); // 访问主张
		
		return payload;
	}

	public static Claims parseJWT(String base64Secret, String token) {
		Claims claims;
		try {
			// 解析jwt串 :其中parseClaimsJws验证jwt字符串失败可能会抛出异常，需要捕获异常
			claims = Jwts.parser().setSigningKey(base64Secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			// jwt 解析错误
			claims = null;
		}
		return claims;
	}

	public String genAccessToken(String signatureAlgorithm, String base64Secret, String uid, String subject,
			String issuer, String audience, Map<String, Object> claims, long access_token_expiration) {
		return jwtBuilder(uid, subject, issuer, audience, claims, access_token_expiration)
				// 压缩，可选GZIP
				.compressWith(CompressionCodecs.DEFLATE)
				// 设置算法（必须）
				.signWith(SignatureAlgorithm.forName(signatureAlgorithm), base64Secret).compact();
	}

	public String genRefreshToken(String signatureAlgorithm, String base64Secret, String uid, String subject,
			String issuer, String audience, Map<String, Object> claims, long refresh_token_expiration) {
		return jwtBuilder(uid, subject, issuer, audience, claims, refresh_token_expiration)
				// 压缩，可选GZIP
				.compressWith(CompressionCodecs.DEFLATE)
				// 设置算法（必须）
				.signWith(SignatureAlgorithm.forName(signatureAlgorithm), base64Secret).compact();
	}

	public Boolean canTokenBeRefreshed(String base64Secret, String token, Date lastPasswordReset) {
		final Date created = getCreatedDateFromToken(base64Secret, token);
		return !isCreatedBeforeLastPasswordReset(created, lastPasswordReset) && (!isTokenExpired(base64Secret, token));
	}

	public String refreshToken(String signatureAlgorithm, String base64Secret, String token,
			long access_token_expiration) {
		String refreshedToken;
		try {
			final Claims claims = parseJWT(base64Secret, token);
			Iterator<Entry<String, Object>> ite = claims.entrySet().iterator();
			Map<String, Object> claimMap = new HashMap<String, Object>();
			while (ite.hasNext()) {
				Entry<String, Object> entry = ite.next();
				claimMap.put(entry.getKey(), entry.getValue());
			}
			refreshedToken = genAccessToken(signatureAlgorithm, base64Secret, claims.getId(), claims.getSubject(),

					claims.getIssuer(), claims.getAudience(), claimMap, access_token_expiration);
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}

	public long getUserIdFromToken(String base64Secret, String token) {
		long userId;
		try {
			final Claims claims = parseJWT(base64Secret, token);
			userId = (Long) claims.get(CLAIM_KEY_USER_ID);
		} catch (Exception e) {
			userId = 0;
		}
		return userId;
	}

	public String getUsernameFromToken(String base64Secret, String token) {
		String username;
		try {
			final Claims claims = parseJWT(base64Secret, token);
			username = claims.getSubject();
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	public Date getCreatedDateFromToken(String base64Secret, String token) {
		Date created;
		try {
			final Claims claims = parseJWT(base64Secret, token);
			created = claims.getIssuedAt();
		} catch (Exception e) {
			created = null;
		}
		return created;
	}

	public static Date getExpirationDateFromToken(String base64Secret, String token) {
		Date expiration;
		try {
			final Claims claims = parseJWT(base64Secret, token);
			expiration = claims.getExpiration();
		} catch (Exception e) {
			expiration = null;
		}
		return expiration;
	}


	public static Boolean isTokenExpired(String base64Secret, String token) {
		final Date expiration = getExpirationDateFromToken(base64Secret, token);
		return expiration.before(new Date());
	}

	public static Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}

}
