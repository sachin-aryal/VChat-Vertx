import io.vertx.core.json.JsonObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class OAuth {
    private final static String consumerKey = "8n4Fm4FVUX289HbhwIavKV59V";
    private final static String consumerSecret = "vVkl6RfKPEgsff3Kz8SngDOaQ9gDfmnyk93EPwwmpJJgffhrOw";

    public static JsonObject getAuthorizationUrl() {
        Twitter twitter = TwitterFactory.getSingleton();
        String returnUrl = "http://localhost:8080/result";
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        JsonObject jsonObject = new JsonObject();
        String authorizationURL = null;
        try {
            RequestToken requestToken = twitter.getOAuthRequestToken(returnUrl);
            try {
                String tokenKeyGen = requestToken.getToken();
                String tokenSecretKeyGen = requestToken.getTokenSecret();
                authorizationURL = requestToken.getAuthorizationURL();
                jsonObject.put("tokenKey",tokenKeyGen);
                jsonObject.put("tokenKeySecret",tokenSecretKeyGen);
                jsonObject.put("authorizationUrl",authorizationURL);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static User oResult(String tokenKeyGen,String tokenSecretKeyGen,String verifier) {
        System.out.println("Redirecting to the logged in url");

        Twitter twitter = new TwitterFactory().getInstance();

        twitter.setOAuthConsumer(consumerKey, consumerSecret);
//        String verifier = routingContext.request().getParam("oauth_verifier");
        RequestToken requestToken = new RequestToken(tokenKeyGen, tokenSecretKeyGen);
        AccessToken accessToken = null;
        User user = null;
        try {
            accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
            twitter.setOAuthAccessToken(accessToken);
            user = twitter.verifyCredentials();

        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return user;
    }
}


