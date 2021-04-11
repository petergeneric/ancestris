/*
 * Copyright (C) 2020 Zurga
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ancestris.modules.exports.geneanet.utils;

import ancestris.modules.exports.geneanet.entity.GeneanetMedia;
import ancestris.modules.exports.geneanet.entity.GeneanetParserResult;
import ancestris.modules.exports.geneanet.entity.GeneanetToken;
import ancestris.modules.exports.geneanet.entity.GenenaetIndiId;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * UÂ§til class to manage any connexion and informaiton exchange with Geneanet.
 *
 * @author Zurga
 */
public class GeneanetUtil {

    private static final String GENEANET_API = "https://api.geneanet.org";
    private static final String GENEANET_TOKEN = GENEANET_API + "/oauth/v2/token";
    private static final String GENEANET_USER_INFO = GENEANET_API + "/media/user";
    private static final String GENEANET_UPLOAD = GENEANET_API + "/geneweb/tree/upload";
    private static final String GENEANET_STATUS = GENEANET_API + "/geneweb/tree/lock/status";
    private static final String GENEANET_MEDIA_PARSER = GENEANET_API + "/geneweb/gedcom/media-parser";
    private static final String GENEANET_DEPOSIT = GENEANET_API + "/media/deposits";
    private final static Logger LOG = Logger.getLogger("ancestris.app", null);

    /**
     * Util pattern, static methods only, no available constructor.
     */
    private GeneanetUtil() {
        super();
    }

    /**
     * Connect to Geneanet and get the API Token.
     *
     * @param id user id
     * @param pwd user pwd
     * @param clientId Ancestris clientId
     * @param secret Ancestris Secret
     * @return new Token.
     */
    public static GeneanetToken getToken(String id, String pwd, String clientId, String secret) throws GeneanetException {
        // create body parameters.
        final JSONObject body = new JSONObject();
        body.put("username", id);
       body.put("password", pwd);
        body.put("grant_type", "password");
        body.put("client_id", clientId);
        body.put("client_secret", secret);

        final JSONObject jToken = post(GENEANET_TOKEN, new StringEntity(body.toString(), ContentType.APPLICATION_JSON), null, "token.error.message");
        final GeneanetToken retour = new GeneanetToken(jToken.getString("access_token"), new Date(), clientId, secret);
        retour.setRefreshToken(jToken.getString("refresh_token"));
        return retour;
    }

    /**
     * Get user info and check if there is a tree online.
     *
     * @param token the token
     * @return true if the account have a tree we can replace.
     * @throws GeneanetException if error.
     */
    public static boolean getUserInfo(GeneanetToken token) throws GeneanetException {
        checkRefreshToken(token);
        final JSONObject userInfo = get(GENEANET_USER_INFO, new ArrayList<>(), token.getToken(), "userinfo.error.message");
        JSONObject user = userInfo.getJSONObject("user");
        return user.getBoolean("tree");
    }

    /**
     * Send GEDCOM file.
     *
     * @param token the Token
     * @param file GEDCOM file
    * @throws GeneanetException if error
     */
    public static void sendFile(GeneanetToken token, File file) throws GeneanetException {
        checkRefreshToken(token);
        HttpEntity reqEntity = MultipartEntityBuilder.create().addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, file.getName()).build();
        final JSONObject retour = post(GENEANET_UPLOAD, reqEntity, token.getToken(), "tree.error.message");
    }

    public static GeneanetUpdateStatus getStatus(GeneanetToken token) throws GeneanetException {
        checkRefreshToken(token);
        final List<NameValuePair> param = new ArrayList<>(1);
       param.add(new BasicNameValuePair("release", "true"));
        JSONObject status = get(GENEANET_STATUS, param, token.getToken(), "status.error.message");
        return new GeneanetUpdateStatus(status.getString("status"), status.getString("action"), status.getString("step"));
    }

    public static GeneanetParserResult getMediaStatus(GeneanetToken token) throws GeneanetException {
        checkRefreshToken(token);
        JSONObject status = get(GENEANET_MEDIA_PARSER, new ArrayList<>(), token.getToken(), "media.status.error.message");
        final GeneanetParserResult pResult = new GeneanetParserResult(status.getInt("nb_media"), status.getInt("nb_ind"));
        JSONObject okMedia = status.getJSONObject("media");
        Set<String> keys = okMedia.keySet();
        for (String key : keys) {
            JSONArray ids = okMedia.getJSONArray(key);
            List<GenenaetIndiId> lIds = new ArrayList<>();
            for (int j = 0; j < ids.length(); j++) {
                lIds.add(new GenenaetIndiId(ids.getString(j)));
           }
            pResult.addOkMedia(new GeneanetMedia(key, lIds));
        }

        JSONArray koMedia = status.getJSONArray("error");
        for (int i = 0; i < koMedia.length(); i++) {
            pResult.addKoMedia(koMedia.getString(i));
        }

        return pResult;
    }

    public static void sendMedia(GeneanetToken token, GeneanetMedia media) throws GeneanetException {
        checkRefreshToken(token);

       MultipartEntityBuilder meb = MultipartEntityBuilder.create();
        meb.addTextBody("deposit[type]", media.getType().getType()).addTextBody("deposit[private]", String.valueOf(media.getPrive()));
        if (media.getTitle() != null) {
            meb.addTextBody("deposit[title]", media.getTitle(), ContentType.create("text/plain", "UTF-8"));
        }
        HttpEntity reqEntity = meb.addBinaryBody("deposit[views][][uploadedFile]", media.getFichier(), ContentType.APPLICATION_OCTET_STREAM, media.getFichier().getName()).build();
        final JSONObject retour = post(GENEANET_DEPOSIT, reqEntity, token.getToken(), "media.deposit.error");
        media.setDepositId(String.valueOf(retour.getLong("id")));
        JSONArray views = retour.getJSONArray("views");
        if (views.length() > 0) {
            JSONObject view = views.getJSONObject(0);
            media.setViewsId(String.valueOf(view.getLong("id")));
       }
    }
    
    public static void referenceMedia(GeneanetToken token, GeneanetMedia media) throws GeneanetException {
        checkRefreshToken(token);
        for (GenenaetIndiId idGedcom : media.getIds()) {
            JSONObject body = new JSONObject();
            body.put("id_gedcom", idGedcom.getId());
            final JSONObject retour = post(GENEANET_DEPOSIT+"/"+media.getDepositId()+"/views/"+media.getViewsId()+"/references", 
                    new StringEntity(body.toString(),  ContentType.APPLICATION_JSON), token.getToken(), "reference.error.message");
        }
        
        
    }

    private static void checkRefreshToken(GeneanetToken token) throws GeneanetException {
        if (token.isExpired()) {
            // create body parameters.
            final JSONObject body = new JSONObject();
            body.put("refresh_token", token.getRefreshToken());
            body.put("grant_type", "refresh_token");
            body.put("client_id", token.getClientId());
            body.put("client_secret", token.getSecretId());

            final JSONObject jToken = post(GENEANET_TOKEN, new StringEntity(body.toString(), ContentType.APPLICATION_JSON), null, "token.error.message");
           token.setToken(jToken.getString("access_token"));
            token.setRefreshToken(jToken.getString("refresh_token"));
            token.setBeginning(new Date());
        }
    }

    // Post action.
    private static JSONObject post(String url, HttpEntity body, String leToken, String errorMessage) throws GeneanetException {
        try (CloseableHttpClient client = HttpClients.createDefault();) {
            HttpPost post = new HttpPost(url);
            post.setEntity(body);
            post.setHeader(HttpHeaders.ACCEPT, "application/json");
            post.setHeader(HttpHeaders.USER_AGENT, "Ancestris");
            if (leToken != null && !"".equals(leToken)) {
                post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + leToken);
            }

            try (CloseableHttpResponse response = client.execute(post);) {
                if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 201) {
                    LOG.log(Level.SEVERE, "geneanet error : " + response.getStatusLine().getReasonPhrase() + "\n" + EntityUtils.toString(response.getEntity()));
                    throw new GeneanetException(errorMessage, null);
                }
                final String entity = EntityUtils.toString(response.getEntity());
                LOG.log(Level.INFO, "Entity : " + entity);
                // Nothing in return entity but request OK.
                if (entity.length() < 3) {
                    return null;
                }
                return new JSONObject(entity);

            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Unable to execute connection.", e);
            }

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to create httpCLient", e);
        }
        throw new GeneanetException(errorMessage, null);
    }

    // Get Action
    private static JSONObject get(String url, List<NameValuePair> parameters, String leToken, String errorMessage) throws GeneanetException {
        try (CloseableHttpClient client = HttpClients.createDefault();) {
            HttpGet get = new HttpGet(url);
            if (!parameters.isEmpty()) {
                URI uri = new URIBuilder(get.getURI()).addParameters(parameters).build();
                get.setURI(uri);
            }
            get.setHeader(HttpHeaders.ACCEPT, "application/json");
            get.setHeader(HttpHeaders.USER_AGENT, "Ancestris");
            get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + leToken);
            try (CloseableHttpResponse response = client.execute(get);) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    LOG.log(Level.SEVERE, "geneanet error : " + response.getStatusLine().getReasonPhrase() + "\n" + EntityUtils.toString(response.getEntity()));
                    throw new GeneanetException(errorMessage, null);
                }
                final String entity = EntityUtils.toString(response.getEntity());
                LOG.log(Level.INFO, "Entity : " + entity);
                return new JSONObject(entity);

            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Unable to execute connection.", e);
            }
        } catch (IOException | URISyntaxException e) {
            LOG.log(Level.SEVERE, "Unable to create httpCLient", e);
        }
        throw new GeneanetException(errorMessage, null);
    }

}
