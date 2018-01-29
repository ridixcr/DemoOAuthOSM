package org.x.demo;

import org.x.oauth.OAuthToken;
import org.openstreetmap.josm.tools.Logging;
import org.x.util.OAuthClient;
import org.x.util.SessionId;

/**
 *
 * @author RidixCr
 */
public class Demo {
    public static String USER="username";
    public static String PASSWORD="password";
    public static void demo1() throws Exception {
        //<editor-fold defaultstate="collapsed" desc="Demo 1">   
        Logging.info("\nDEMO 1");
        OAuthClient c = new OAuthClient();
        SessionId sessionId = c.login(USER,PASSWORD);

        OAuthToken requestToken = c.getRequestToken();
        Logging.warn(requestToken.toString());
        
        c.getOAuthToken(sessionId, requestToken);
        Logging.warn("Token : "+sessionId.getToken());
        
        c.logout(sessionId);
        //</editor-fold>
    }
    public static void demo2() throws Exception {
        //<editor-fold defaultstate="collapsed" desc="Demo 2">
        Logging.info("\nDEMO 2");
        OAuthClient c = new OAuthClient();
        
        OAuthToken requestToken = c.getRequestToken();
        Logging.warn(requestToken.toString());
                
        c.authorise(requestToken,USER,PASSWORD, c.getPrivileges());
        //</editor-fold>
    }
    public static void main(String[] args) throws Exception {
        USER="demouser";
        PASSWORD="demopassword";
        demo1();
        demo2();
    }
}
