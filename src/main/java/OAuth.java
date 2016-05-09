import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

/**
 * Author: SACHIN
 * Date: 5/6/2016.
 */
public class OAuth {
    private final String consumerKey = "8n4Fm4FVUX289HbhwIavKV59V";
    private final String consumerSecret = "vVkl6RfKPEgsff3Kz8SngDOaQ9gDfmnyk93EPwwmpJJgffhrOw";

    public static String getAuthorizationUrl(){
        Twitter twitter = TwitterFactory.getSingleton();
        String returnUrl = "http://localhost:8080/result";
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        String authorizationURL = null;
        try {
            RequestToken requestToken = twitter.getOAuthRequestToken(returnUrl);
            try {
                String tokenKeyGen = requestToken.getToken();
                String tokenSecretKeyGen = requestToken.getTokenSecret();
                authorizationURL = requestToken.getAuthorizationURL();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return authorizationURL;
    }
}
