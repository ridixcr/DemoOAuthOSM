package org.x.util;
//<editor-fold defaultstate="collapsed" desc="Dependencias">
import org.x.oauth.OAuthParameters;
import org.x.oauth.OsmPrivileges;
import org.x.oauth.OAuthToken;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import oauth.signpost.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.io.OsmApi;
import org.openstreetmap.josm.tools.CheckParameterUtil;
import static org.openstreetmap.josm.tools.I18n.tr;
import org.openstreetmap.josm.tools.*;
//</editor-fold>
/**
 *
 * @author RidixCr
 */
public class OAuthClient {
    //<editor-fold defaultstate="collapsed" desc="Variable">
    static String API_URL = OsmApi.DEFAULT_API_URL;
    static String OSM_WEBSITE = Main.getOSMWebsite();

    private OAuthParameters oauthProviderParameters;
    private OAuthConsumer consumer;
    private OAuthProvider provider;
    private OAuthParameters parameters;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Constructores">
    public OAuthClient() {
        //<editor-fold defaultstate="collapsed" desc="comment">
        parameters = OAuthParameters.createDefault(API_URL);
        CheckParameterUtil.ensureParameterNotNull(parameters, "parameters");
        oauthProviderParameters = new OAuthParameters(parameters);
        consumer = oauthProviderParameters.buildConsumer();
        provider = oauthProviderParameters.buildProvider(consumer);
        //</editor-fold>
    }

    public OAuthClient(OAuthToken requestToken) {
        //<editor-fold defaultstate="collapsed" desc="comment">
        parameters = OAuthParameters.createDefault(API_URL);
        CheckParameterUtil.ensureParameterNotNull(parameters, "parameters");
        oauthProviderParameters = new OAuthParameters(parameters);
        consumer = oauthProviderParameters.buildConsumer();
        provider = oauthProviderParameters.buildProvider(consumer);
        consumer.setTokenWithSecret(requestToken.getKey(), requestToken.getSecret());
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Funciones">
    public static OsmPrivileges getPrivileges() {
        //<editor-fold defaultstate="collapsed" desc="comment">
        OsmPrivileges privileges = new OsmPrivileges();
        privileges.setAllowWriteApi(true);
        privileges.setAllowWriteGpx(true);
        privileges.setAllowReadGpx(true);
        privileges.setAllowWritePrefs(true);
        privileges.setAllowReadPrefs(true);
        privileges.setAllowModifyNotes(true);
        return privileges;
        //</editor-fold>
    }

    public OAuthToken getRequestToken() throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        try {
            //System.out.println(oauthProviderParameters.getRequestTokenUrl());
            provider.retrieveRequestToken(consumer, "");            
            return OAuthToken.createToken(consumer);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            Logging.info("Finish RequestToken!!!");
        }
        //</editor-fold>
    }

    private String getAuthoriseUrl(OAuthToken requestToken) {
        //<editor-fold defaultstate="collapsed" desc="comment">
        StringBuilder sb = new StringBuilder(32);

        // OSM is an OAuth 1.0 provider and JOSM isn't a web app. We just add the oauth request token to
        // the authorisation request, no callback parameter.
        //
        sb.append(oauthProviderParameters.getAuthoriseUrl()).append('?' + OAuth.OAUTH_TOKEN + '=').append(requestToken.getKey());
        return sb.toString();
        //</editor-fold>
    }

    public void getOAuthToken(SessionId sessionId, OAuthToken requestToken) throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        HttpClient connection = null;
        try {
            URL url = new URL(getAuthoriseUrl(requestToken));
            synchronized (this) {
                connection = HttpClient.create(url)
                        .useCache(false)
                        .setHeader("Cookie", "_osm_session=" + sessionId.getId() + "; _osm_username=" + sessionId.getUserName());
                connection.connect();
            }
            //System.out.println(connection.getResponse().getContentType());
            sessionId.setToken(extractToken(connection));
            if (sessionId.getToken() == null) {
                throw new Exception(tr("OSM website did not return a session cookie in response to ''{0}'',",
                        url.toString()));
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            synchronized (this) {
                connection = null;
            }
            Logging.info("Finish OAuthToken!!!");
        }
        //</editor-fold>
    }

    private SessionId getOsmWebsiteSessionId(String u) throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        HttpClient connection = null;
        SessionId sessionId;
        try {
            URL url = new URL(oauthProviderParameters.getOsmLoginUrl() + "?cookie_test=true");
            synchronized (this) {
                connection = HttpClient.create(url).useCache(false);
                connection.connect();
            }
            sessionId = extractOsmSession(connection);
            if (sessionId == null) {
                throw new Exception(tr("OSM website did not return a session cookie in response to ''{0}'',", url.toString()));
            }
            sessionId.setUserName(u);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            synchronized (this) {
                if (connection != null) {
                    connection.disconnect();
                    connection = null;
                }

            }
        }
        return sessionId;
        //</editor-fold>
    }

    private static String buildPostRequest(Map<String, String> parameters) {
        //<editor-fold defaultstate="collapsed" desc="comment">
        StringBuilder sb = new StringBuilder(32);

        for (Iterator<Map.Entry<String, String>> it = parameters.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            String value = entry.getValue();
            value = (value == null) ? "" : value;
            sb.append(entry.getKey()).append('=').append(Utils.encodeUrl(value));
            if (it.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
        //</editor-fold>
    }

    public SessionId login(String userName, String password) throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        HttpClient connection;
        SessionId sessionId=null;
        try {
            URL url = new URL(oauthProviderParameters.getOsmLoginUrl());
            connection = HttpClient.create(url, "POST").useCache(false);
            sessionId = getOsmWebsiteSessionId(userName);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("username", userName);
            parameters.put("password", password);
            parameters.put("referer", "/");
            parameters.put("commit", "Login");
            parameters.put("authenticity_token", sessionId.getToken());
            connection.setRequestBody(buildPostRequest(parameters).getBytes(StandardCharsets.UTF_8));

            connection.setHeader("Content-Type", "application/x-www-form-urlencoded");
            connection.setHeader("Cookie", "_osm_session=" + sessionId.getId());
            // make sure we can catch 302 Moved Temporarily below
            connection.setMaxRedirects(-1);

            synchronized (this) {
                connection.connect();
            }

            // after a successful login the OSM website sends a redirect to a follow up page. Everything
            // else, including a 200 OK, is a failed login. A 200 OK is replied if the login form with
            // an error page is sent to back to the user.
            //
            int retCode = connection.getResponse().getResponseCode();
            if (retCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                throw new Exception(tr("Failed to authenticate user ''{0}'' with password ''***'' as OAuth user",
                        userName));
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            synchronized (this) {
                connection = null;
            }
        }
        return sessionId;
        //</editor-fold>
    }

    public void logout(SessionId sessionId) throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        HttpClient connection;
        try {
            URL url = new URL(oauthProviderParameters.getOsmLogoutUrl());
            synchronized (this) {
                connection = HttpClient.create(url).setMaxRedirects(-1);
                connection.connect();
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            synchronized (this) {
                connection = null;
            }
            Logging.info("Logout OSM Session!!!");
        }
        //</editor-fold>
    }
    
    private SessionId extractOsmSession(HttpClient connection) throws IOException, URISyntaxException {
        //<editor-fold defaultstate="collapsed" desc="comment">
        // response headers might not contain the cookie, see #12584
        final List<String> setCookies = CookieHandler.getDefault()
                .get(connection.getURL().toURI(), Collections.<String, List<String>>emptyMap())
                .get("Cookie");
        if (setCookies == null) {
            Logging.warn("No 'Set-Cookie' in response header!");
            return null;
        }

        for (String setCookie : setCookies) {
            String[] kvPairs = setCookie.split(";");
            if (kvPairs.length == 0) {
                continue;
            }
            for (String kvPair : kvPairs) {
                kvPair = kvPair.trim();
                String[] kv = kvPair.split("=");
                if (kv.length != 2) {
                    continue;
                }
                if ("_osm_session".equals(kv[0])) {
                    // osm session cookie found
                    String token = extractToken(connection);
                    if (token == null) {
                        return null;
                    }
                    SessionId si = new SessionId();
                    si.setId(kv[1]);
                    si.setToken(token);
                    return si;
                }
            }
        }
        Logging.warn("No suitable 'Set-Cookie' in response header found! {0}", setCookies);
        return null;
        //</editor-fold>
    }

    private String extractToken(HttpClient connection) {
        //<editor-fold defaultstate="collapsed" desc="comment">
        try (BufferedReader r = connection.getResponse().getContentReader()) {
            String c;
            Pattern p = Pattern.compile(".*authenticity_token.*value=\"([^\"]+)\".*");
            while ((c = r.readLine()) != null) {
                Matcher m = p.matcher(c);
                if (m.find()) {
                    return m.group(1);
                }
            }
        } catch (IOException e) {
            Logging.error(e);
            return null;
        }
        Logging.warn("No authenticity_token found in response!");
        return null;
        //</editor-fold>
    }   

    private void sendAuthorisationRequest(SessionId sessionId, OAuthToken requestToken, OsmPrivileges privileges)throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        HttpClient connection;
        Map<String, String> parameters = new HashMap<>();
        getOAuthToken(sessionId, requestToken);
        parameters.put("oauth_token", requestToken.getKey());
        parameters.put("oauth_callback", "");
        parameters.put("authenticity_token", sessionId.getToken());
        if (privileges.isAllowWriteApi()) {
            parameters.put("allow_write_api", "yes");
        }
        if (privileges.isAllowWriteGpx()) {
            parameters.put("allow_write_gpx", "yes");
        }
        if (privileges.isAllowReadGpx()) {
            parameters.put("allow_read_gpx", "yes");
        }
        if (privileges.isAllowWritePrefs()) {
            parameters.put("allow_write_prefs", "yes");
        }
        if (privileges.isAllowReadPrefs()) {
            parameters.put("allow_read_prefs", "yes");
        }
        if (privileges.isAllowModifyNotes()) {
            parameters.put("allow_write_notes", "yes");
        }

        parameters.put("commit", "Save changes");

        String request = buildPostRequest(parameters);
        try {
            URL url = new URL(oauthProviderParameters.getAuthoriseUrl());
            connection = HttpClient.create(url, "POST").useCache(false);
            connection.setHeader("Content-Type", "application/x-www-form-urlencoded");
            connection.setHeader("Cookie", "_osm_session=" + sessionId.getId() + "; _osm_username=" + sessionId.getUserName());
            connection.setMaxRedirects(-1);
            connection.setRequestBody(request.getBytes(StandardCharsets.UTF_8));

            synchronized (this) {
                connection.connect();
            }

            int retCode = connection.getResponse().getResponseCode();
            if (retCode != HttpURLConnection.HTTP_OK)
                throw new Exception(tr("Failed to authorize OAuth request  ''{0}''", requestToken.getKey()));
        } catch (IOException e) {
            throw new Exception(e);
        } finally {
            synchronized (this) {
                connection = null;
            }
        }
        //</editor-fold>
    }
   
    public void authorise(OAuthToken requestToken, String userName, String password, OsmPrivileges privileges)throws Exception {
        //<editor-fold defaultstate="collapsed" desc="comment">
        CheckParameterUtil.ensureParameterNotNull(requestToken, "requestToken");
        CheckParameterUtil.ensureParameterNotNull(userName, "userName");
        CheckParameterUtil.ensureParameterNotNull(password, "password");
        CheckParameterUtil.ensureParameterNotNull(privileges, "privileges");

        try {
            Logging.info(tr("Authorizing OAuth Request token ''{0}'' at the OSM website ...", requestToken.getKey()));
            Logging.info(tr("Initializing a session at the OSM website...")); 
            SessionId sessionId = login(userName, password);
            Logging.info(tr("Authenticating the session for user ''{0}''...", userName));
            Logging.info(tr("Authorizing request token ''{0}''...", requestToken.getKey()));
            sendAuthorisationRequest(sessionId, requestToken, privileges);            
            Logging.info(tr("Logging out session ''{0}''...", sessionId));
            logout(sessionId);            
        } catch (Exception e) {            
            throw e;
        } finally {
            Logging.info("Finish Authorise!!!");
        }
        //</editor-fold>
    }    
    //</editor-fold>
}
