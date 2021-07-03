package ysj.nifi;

import com.davis.client.model.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.hadoop.fs.InvalidRequestException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import ysj.common.http.HttpCaller;
import ysj.common.http.HttpCallerFactory;
import ysj.common.http.HttpUtil;
import ysj.nifi.model.ClusterSummaryEntity;
import ysj.nifi.model.ControllerHistoryEntity;
import ysj.nifi.model.ProcessGroupFlowEntity;
import ysj.nifi.model.ProcessGroupStatusEntityV2;

import javax.security.auth.login.LoginException;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NifiApiClient {

    public static final String CLUSTER_STATUS_URL = "%s/nifi-api/controller/status/history";
    public static final String CLUSTER_SUMMARY_URL = "%s/nifi-api/flow/cluster/summary";
    public static final String PROCESS_GROUPS_URL = "%s/nifi-api/flow/process-groups/%s";
    public static final String PROCESS_HIST_URL = "%s/nifi-api/flow/processors/%s/status/history";
    public static final String PROCESS_GROUP_STATUS_URL = "%s/nifi-api/flow/process-groups/%s/status";
    public static final String CONNECTION_DETAIL_URL = "%s/nifi-api/flow/connections/%s/status";
    public static final String FLOW_SEARCH_RESULT_URL = "%s/nifi-api/flow/search-results?q=%s";
    public static final String PROCESSOR_DETAIL_URL = "%s/nifi-api/processors/%s";
    public static final String PROCESS_GROUP_DETAIL_URL = "%s/nifi-api/flow/process-groups/%s";
    public static final String KERBEROS_TOKEN_URL = "%s/nifi-api/access/kerberos";
    public static final String LOGIN_TOKEN_URL = "%s/nifi-api/access/token";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String nifiHost;
    private String principle;
    private File keytab;
    private String username;
    private String password;
    private String token;
    private long tokenLastUpdate = 0;


    private static class RequestResponse{
        String response;
        long lastUpdate;
        public RequestResponse(String response, long lastUpdate) {
            this.response = response;
            this.lastUpdate = lastUpdate;
        }
    }
    private Map<String, RequestResponse> responseCache = new HashMap<>();

    public NifiApiClient(String nifiHost) {
        this.nifiHost = nifiHost;
        setupVariables();
    }

    public NifiApiClient(String nifiHost, String principle, File keytab) {
        this.nifiHost = nifiHost;
        this.principle = principle;
        this.keytab = keytab;
        setupVariables();
    }

    public NifiApiClient(String nifiHost, String username, String password) {
        this.nifiHost = nifiHost;
        this.username = username;
        this.password = password;
        setupVariables();
    }

    public void setupVariables(){
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());
    }

    private void updateToken() throws Exception {
        if((System.currentTimeMillis()-tokenLastUpdate)<(1000*60*15) && token!=null && token.length()>0){
            return;
        }
        if(username!=null && password!=null){
            updateToken(username,password);
        }
        if(principle!=null && keytab!=null){
            updateTokenKerberos(principle,keytab);
        }
    }

    private void updateToken(String username, String password) throws Exception {
        String url = String.format(LOGIN_TOKEN_URL, nifiHost);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type", MediaType.APPLICATION_FORM_URLENCODED);
        httpPost.setEntity(new StringEntity(String.format("username=%s&password=%s",username, password)));
        HttpResponse response = HttpCallerFactory.create().execute(httpPost);
        token = HttpUtil.httpEntityToString(response.getEntity());
        tokenLastUpdate = System.currentTimeMillis();
    }


    private void updateTokenKerberos(String principle, File keytab) throws Exception {
        String url = String.format(KERBEROS_TOKEN_URL, nifiHost);
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response = HttpCallerFactory.create(principle,keytab).execute(httpPost);
        token = HttpUtil.httpEntityToString(response.getEntity());
        tokenLastUpdate = System.currentTimeMillis();
    }

    public  ControllerHistoryEntity getClusterStatus() throws IOException, LoginException {
        String url = String.format(CLUSTER_STATUS_URL, nifiHost);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        return objectMapper.readValue(requestJson(url), ControllerHistoryEntity.class);
    }
    private ProcessGroupStatusEntity getProcessGroupStatus(String id) throws IOException, LoginException {
        String url = String.format(PROCESS_GROUP_STATUS_URL, nifiHost, id);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        return objectMapper.readValue(requestJson(url), ProcessGroupStatusEntity.class);
    }

    public ProcessGroupFlowEntity getProcessGroup(String id) throws IOException, LoginException {
        String url = String.format(PROCESS_GROUP_DETAIL_URL, nifiHost, id);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());
        return objectMapper.readValue(requestJson(url), ProcessGroupFlowEntity.class);
    }

    public StatusHistoryEntity getProcessHistoryJson(String id) throws IOException, LoginException {
        String url = String.format(PROCESS_HIST_URL, nifiHost, id);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());
        return objectMapper.readValue(requestJson(url), StatusHistoryEntity.class);
    }


    private ConnectionStatusEntity getProcessConnection(String id) throws IOException, LoginException {
        String url = String.format(CONNECTION_DETAIL_URL, nifiHost,id);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(requestJson(url), ConnectionStatusEntity.class);
    }
    private ProcessorEntity requestProcessorJson(String id) throws IOException, LoginException {
        String url = String.format(PROCESSOR_DETAIL_URL, nifiHost, id);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        return objectMapper.readValue(requestJson(url), ProcessorEntity.class);
    }

    private SearchResultsEntity getSearchResult(String keyword) throws IOException, LoginException {
        String url = String.format(FLOW_SEARCH_RESULT_URL, nifiHost,keyword);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(requestJson(url), SearchResultsEntity.class);
    }

    public ClusterSummaryEntity getClusterInfoJson() throws IOException, LoginException {
        String url = String.format(CLUSTER_SUMMARY_URL, nifiHost);

        return objectMapper.readValue( requestJson(url), ClusterSummaryEntity.class);
    }

    public ProcessGroupStatusEntityV2 getSummary() throws IOException, LoginException {
        String url = String.format(PROCESS_GROUP_STATUS_URL, nifiHost, "root")+"?recursive=true";
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .registerModule(new JodaModule());

        return objectMapper.readValue(requestJson(url), ProcessGroupStatusEntityV2.class);
    }

    private String requestJson(String url) throws IOException, LoginException {
        if(responseCache.containsKey(url)
            && System.currentTimeMillis()-responseCache.get(url).lastUpdate<1000*30){
            return responseCache.get(url).response;
        }

        url = URIUtil.encodeQuery(url);
        HttpCaller httpCaller = HttpCallerFactory.create();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("content-type", MediaType.APPLICATION_JSON);
        try {
            updateToken();
            httpGet.addHeader("Authorization","Bearer "+token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpResponse response = httpCaller.execute(httpGet);

        int statusCode = response.getStatusLine().getStatusCode();
        String strResponse="";
        strResponse = HttpUtil.httpEntityToString(response.getEntity());
        if(statusCode != 200){
            throw new InvalidRequestException(strResponse);
        }

        // the timestamp returned could not be parsed, this is a work around to remove the following field as
        String[] fieldsUnparseable = {"generated","statsLastRefreshed","lastRefreshed", "timestamp"};
        for(String field: fieldsUnparseable){
            strResponse=strResponse.replaceAll("\""+field+"\":\"[^\"]*\",","");
            strResponse=strResponse.replaceAll(",\""+field+"\":\"[^\"]*\"","");
        }

        responseCache.put(url, new RequestResponse(strResponse, System.currentTimeMillis()));
        return strResponse;
    }

}
