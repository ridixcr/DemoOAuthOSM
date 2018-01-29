# DemoOAuthOSM
OAuth OpenStreetMap Client

![](https://user-images.githubusercontent.com/1646072/35531447-076d7bee-0505-11e8-9e7c-84b11362d9bb.png)

### Demo 1

```
OAuthClient c = new OAuthClient();
SessionId sessionId = c.login(USER,PASSWORD);
OAuthToken requestToken = c.getRequestToken();
Logging.warn(requestToken.toString());        
c.getOAuthToken(sessionId, requestToken);
Logging.warn("Token : "+sessionId.getToken()); 
c.logout(sessionId);
```

>Output
```
2018-01-29 14:48:29.310 INFORMACIÓN: GET https://www.openstreetmap.org/login?cookie_test=true -> 200 (4,81 kB)
2018-01-29 14:48:29.457 INFORMACIÓN: POST https://www.openstreetmap.org/login (189 B) ...
2018-01-29 14:48:29.710 INFORMACIÓN: POST https://www.openstreetmap.org/login -> 302
2018-01-29 14:48:30.010 INFORMACIÓN: GET https://www.openstreetmap.org/oauth/request_token -> 200 (124 B)
2018-01-29 14:48:30.126 INFORMACIÓN: Finish RequestToken!!!
2018-01-29 14:48:30.126 ADVERTENCIA: OAuthToken{key=EUZ8CRodBq64xu1qs23eNSrQnaVdxHfjv9EpOVOm, secret=k5ss0QrsUbisyo2Lq1t5d36vLDjouyty8vAx8dWG}
2018-01-29 14:48:30.975 INFORMACIÓN: GET https://www.openstreetmap.org/oauth/authorize?oauth_token=EUZ8CRodBq64xuNJ8HSeNSrQnaVdxHfjv9EpOVOm -> 200 (4,12 kB)
2018-01-29 14:48:30.990 INFORMACIÓN: Finish OAuthToken!!!
2018-01-29 14:48:30.990 ADVERTENCIA: Token : vRjlP2300E8rvRRB8K6faOmJojqF7EAMwZGwIoPI123467caU6xC/p8OBNMXMOrbkdWbzVQlkIpSHFPLDHBtwyw==
2018-01-29 14:48:31.444 INFORMACIÓN: GET https://www.openstreetmap.org/logout -> 200 (3,72 kB)
2018-01-29 14:48:31.444 INFORMACIÓN: Logout OSM Session!!!
```

### Demo 2

```
OAuthClient c = new OAuthClient();        
OAuthToken requestToken = c.getRequestToken();
Logging.warn(requestToken.toString());                
c.authorise(requestToken,USER,PASSWORD, c.getPrivileges());
```

>Output
```
2018-01-29 14:48:32.096 INFORMACIÓN: GET https://www.openstreetmap.org/oauth/request_token -> 200 (124 B)
2018-01-29 14:48:32.196 INFORMACIÓN: Finish RequestToken!!!
2018-01-29 14:48:32.196 ADVERTENCIA: OAuthToken{key=fdSI8XyHwS1dy9k3TYdc1sdMHj35FO84x6dZBgOxZ, secret=dTUUWhDHJnwYhNBHr61sdfrtujY9oXwkjgxkwpVHdQx}
2018-01-29 14:48:32.196 INFORMACIÓN: Authorizing OAuth Request token 'fdSI8XyHwS5xiagyu945dMHj35FO84x6dZBgOxZ' at the OSM website ...
2018-01-29 14:48:32.196 INFORMACIÓN: Initializing a session at the OSM website...
2018-01-29 14:48:33.041 INFORMACIÓN: GET https://www.openstreetmap.org/login?cookie_test=true -> 200 (5,07 kB)
2018-01-29 14:48:33.157 INFORMACIÓN: POST https://www.openstreetmap.org/login (183 B) ...
2018-01-29 14:48:33.424 INFORMACIÓN: POST https://www.openstreetmap.org/login -> 302
2018-01-29 14:48:33.426 INFORMACIÓN: Authenticating the session for user 'ridixcr'...
2018-01-29 14:48:33.426 INFORMACIÓN: Authorizing request token 'fdSI8XyHwS5xih15c29p4dMHj35FO84x6dZBgOxZ'...
2018-01-29 14:48:33.688 INFORMACIÓN: GET https://www.openstreetmap.org/oauth/authorize?oauth_token=fdSI8XyHwS5xih6Tak928f6dMHj35FO84x6dZBgOxZ -> 200 (4,11 kB)
2018-01-29 14:48:33.726 INFORMACIÓN: Finish OAuthToken!!!
2018-01-29 14:48:33.726 INFORMACIÓN: POST https://www.openstreetmap.org/oauth/authorize (334 B) ...
2018-01-29 14:48:34.026 INFORMACIÓN: POST https://www.openstreetmap.org/oauth/authorize -> 200 (3,60 kB)
2018-01-29 14:48:34.026 INFORMACIÓN: Logging out session 'SessionId{id=2a31d7ee0dc1508fe06f03e211c22261, token=lHH0F3hnEMPhCnFPLoNnr2Y5nPBwyZOxG1jus1847o0ScNe8/oN/Kwk2UcsSF04j+tbNn/xB8SlcIcY0Byhl/Q==, userName=ridixcr}'...
2018-01-29 14:48:35.121 INFORMACIÓN: GET https://www.openstreetmap.org/logout -> 200 (3,72 kB)
2018-01-29 14:48:35.121 INFORMACIÓN: Logout OSM Session!!!
2018-01-29 14:48:35.121 INFORMACIÓN: Finish Authorise!!!
```
