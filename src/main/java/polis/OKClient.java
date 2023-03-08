package polis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import polis.domain.Attachment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OKClient {
    private static final String POST_MEDIA_TOPIC = "mediatopic.post";
    private static final String UPLOAD_PHOTO = "photosV2.getUploadUrl";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void postMediaTopic(String accessToken, long groupId, Attachment attachment) throws IOException, InterruptedException, URISyntaxException {
        URI uri = new URIBuilder("https://api.ok.ru/fb.do")
                .addParameter("application_key", OkAuthorization.APPLICATION_KEY)
                .addParameter("attachment", mapper.writeValueAsString(attachment))
                .addParameter("format", "json")
                .addParameter("gid", String.valueOf(groupId))
                .addParameter("method", POST_MEDIA_TOPIC)
                .addParameter("type", "GROUP_THEME")
                .addParameter("sig", OkAuthorization.sig(accessToken, POST_MEDIA_TOPIC))
                .addParameter("access_token", accessToken)
                .build();
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(POST_MEDIA_TOPIC + " response: ");
        System.out.println(response.body());
    }

    /**
     * @return photoIds
     */
    public Collection<String> uploadPhotos(String accessToken, long groupId, List<File> photos) throws URISyntaxException, IOException, InterruptedException {
        JSONObject object = new JSONObject(getUploadUrl(accessToken, groupId, photos));
        JSONArray photoTokens = object.getJSONArray("photo_ids");
        String uploadUrl = object.getString("upload_url");

        org.apache.http.client.HttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(URI.create(uploadUrl));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (int i = 0; i < photos.size(); i++) {
            File photo = photos.get(i);
            multipartEntityBuilder.addPart("pic" + (i + 1), new FileBody(photo));
        }
        httpPost.setEntity(multipartEntityBuilder.build());
        org.apache.http.HttpResponse response = httpclient.execute(httpPost);

        String responseString = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        JSONObject photoIds = new JSONObject(responseString).getJSONObject("photos");

        List<String> result = new ArrayList<>(photos.size());
        for (int i = 0; i < photos.size(); i++) {
            String photoToken = photoTokens.getString(i);
            String photoId = photoIds.getJSONObject(photoToken).getString("token");
            result.add(photoId);
        }
        return result;
    }

    private String getUploadUrl(String accessToken, long groupId, List<File> photos) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URIBuilder("https://api.ok.ru/fb.do")
                .addParameter("application_key", OkAuthorization.APPLICATION_KEY)
                .addParameter("count", String.valueOf(photos.size()))
                .addParameter("format", "json")
                .addParameter("gid", String.valueOf(groupId))
                .addParameter("method", UPLOAD_PHOTO)
                .addParameter("sig", OkAuthorization.sig(accessToken, UPLOAD_PHOTO))
                .addParameter("access_token", accessToken)
                .build();

        HttpRequest getUploadUrlRequest = HttpRequest.newBuilder().GET()
                .uri(uri)
                .build();

        HttpResponse<String> uploadUrlResponse = client.send(getUploadUrlRequest, HttpResponse.BodyHandlers.ofString());
        return uploadUrlResponse.body();
    }
}
