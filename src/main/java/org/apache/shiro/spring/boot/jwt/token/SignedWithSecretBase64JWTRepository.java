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
package org.apache.shiro.spring.boot.jwt.token;

import java.text.ParseException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.spring.boot.jwt.JwtPlayload;
import org.apache.shiro.spring.boot.utils.JJwtUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodec;
import io.jsonwebtoken.CompressionCodecResolver;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JSON Web Token (JWT) with signature <br/>
 * https://github.com/jwtk/jjwt <br/>
 * 私钥签名，公钥验证
 */
public class SignedWithSecretBase64JWTRepository implements JwtRepository<String> {

	private long allowedClockSkewSeconds = -1;
	private CompressionCodec compressWith = CompressionCodecs.DEFLATE;
    private CompressionCodecResolver compressionCodecResolver;
    
	/**
	 * 
	 * @author ：<a href="https://github.com/vindell">vindell</a>
	 * @param id
	 * @param subject
	 * @param issuer
	 * @param period
	 * @param roles
	 * @param permissions
	 * @param algorithm: <br/>
	 *  HS256: HMAC using SHA-256 <br/>
	 *  HS384: HMAC using SHA-384 <br/>
     *  HS512: HMAC using SHA-512 <br/>
     *  ES256: ECDSA using P-256 and SHA-256 <br/>
     *  ES384: ECDSA using P-384 and SHA-384 <br/>
     *  ES512: ECDSA using P-521 and SHA-512 <br/>
     *  RS256: RSASSA-PKCS-v1_5 using SHA-256 <br/>
     *  RS384: RSASSA-PKCS-v1_5 using SHA-384 <br/>
     *  RS512: RSASSA-PKCS-v1_5 using SHA-512 <br/>
     *  PS256: RSASSA-PSS using SHA-256 and MGF1 with SHA-256 <br/>
     *  PS384: RSASSA-PSS using SHA-384 and MGF1 with SHA-384 <br/>
     *  PS512: RSASSA-PSS using SHA-512 and MGF1 with SHA-512 <br/>
	 * @return JSON Web Token (JWT)
	 * @throws Exception 
	 */
	@Override
	public String issueJwt(String base64Secret, String id, String subject, String issuer, Long period, String roles,
			String permissions, String algorithm)  throws AuthenticationException {
		String token = JJwtUtils
				.jwtBuilder(id, subject, issuer, period, roles, permissions)
				// 压缩类型
				.compressWith(getCompressWith())
				// 设置算法（必须）
				.signWith(SignatureAlgorithm.forName(algorithm), base64Secret).compact();
		return token;
	}

	@Override
	public boolean verify(String base64Secret, String token, boolean checkExpiry) throws AuthenticationException {
			
		try {
			
			// Retrieve / verify the JWT claims according to the app requirements
			JwtParser jwtParser = Jwts.parser();
			// 设置允许的时间误差
			if(getAllowedClockSkewSeconds() > 0) {
				jwtParser.setAllowedClockSkewSeconds(getAllowedClockSkewSeconds());	
			}
			// 设置压缩方式解析器
			if(null != getCompressionCodecResolver() ) {
				jwtParser.setCompressionCodecResolver(getCompressionCodecResolver());
			}
			
			Jws<Claims> jws = jwtParser.setSigningKey(base64Secret).parseClaimsJws(token);

			Claims claims = jws.getBody();

			System.out.println("Expiration:" + claims.getExpiration());
			System.out.println("IssuedAt:" + claims.getIssuedAt());
			System.out.println("NotBefore:" + claims.getNotBefore());
			
			long time = System.currentTimeMillis();
			return claims != null && claims.getNotBefore().getTime() <= time
					&& time < claims.getExpiration().getTime();
			
		} catch(InvalidClaimException e) {
			throw new AuthenticationException(e);
		}
		
	}

	@Override
	public JwtPlayload getPlayload(String base64Secret, String token)  throws AuthenticationException {
		try {
			
			// Retrieve JWT claims
			JwtParser jwtParser = Jwts.parser();
			// 设置允许的时间误差
			if(getAllowedClockSkewSeconds() > 0) {
				jwtParser.setAllowedClockSkewSeconds(getAllowedClockSkewSeconds());	
			}
			Jws<Claims> jws = jwtParser.setSigningKey(base64Secret).parseClaimsJws(token);
			
			return JJwtUtils.playload(jws.getBody());
		} catch (ParseException e) {
			throw new AuthenticationException(e);
		}
	}

	public long getAllowedClockSkewSeconds() {
		return allowedClockSkewSeconds;
	}

	public void setAllowedClockSkewSeconds(long allowedClockSkewSeconds) {
		this.allowedClockSkewSeconds = allowedClockSkewSeconds;
	}

	public CompressionCodec getCompressWith() {
		return compressWith;
	}

	public void setCompressWith(CompressionCodec compressWith) {
		this.compressWith = compressWith;
	}

	public CompressionCodecResolver getCompressionCodecResolver() {
		return compressionCodecResolver;
	}

	public void setCompressionCodecResolver(CompressionCodecResolver compressionCodecResolver) {
		this.compressionCodecResolver = compressionCodecResolver;
	}
	
}