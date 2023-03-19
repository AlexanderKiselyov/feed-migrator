package polis.ok.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import polis.ok.domain.Attachment;

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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OkClientImpl implements OKClient {
    private static final String POST_MEDIA_TOPIC = "mediatopic.post";
    private static final String UPLOAD_PHOTO = "photosV2.getUploadUrl";
    private static final String UPLOAD_VIDEO = "video.getUploadUrl";

    private final org.apache.http.client.HttpClient advancedClient = HttpClientBuilder.create().build();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void postMediaTopic(String accessToken, long groupId, Attachment attachment) throws Exception {
        URI uri = new URIBuilder("https://api.ok.ru/fb.do")
                .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                .addParameter("attachment", mapper.writeValueAsString(attachment))
                .addParameter("format", "json")
                .addParameter("gid", String.valueOf(groupId))
                .addParameter("method", POST_MEDIA_TOPIC)
                .addParameter("type", "GROUP_THEME")
                .addParameter("sig", OkAuthorizator.sig(accessToken, POST_MEDIA_TOPIC))
                .addParameter("access_token", accessToken)
                .build();
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(uri)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(POST_MEDIA_TOPIC + " response: ");
        System.out.println(response.body());
    }

    public List<String> uploadPhotos(String accessToken, long groupId, List<File> photos) throws Exception {
        PhotoUploadUrlResponse uploadUrlResponse = photoUploadUrl(accessToken, groupId, photos);

        HttpPost httpPost = new HttpPost(URI.create(uploadUrlResponse.uploadUrl));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (int i = 0; i < photos.size(); i++) {
            File photo = photos.get(i);
            multipartEntityBuilder.addPart("pic" + (i + 1), new FileBody(photo));
        }
        httpPost.setEntity(multipartEntityBuilder.build());
        org.apache.http.HttpResponse response = advancedClient.execute(httpPost);

        String responseString = responseAsString(response);
        JSONObject photoIds = new JSONObject(responseString).getJSONObject("photos");
        List<String> result = new ArrayList<>(photos.size());
        for (int i = 0; i < photos.size(); i++) {
            String photoToken = uploadUrlResponse.photoTokens.get(i);
            String photoId = photoIds.getJSONObject(photoToken).getString("token");
            result.add(photoId);
        }
        return result;
    }

    public long uploadVideo(String accessToken, long groupId, File video) throws Exception {
        VideoUploadUrlResponse uploadUrlResponse = videoUploadUrl(accessToken, groupId, video.getName(), video.getTotalSpace());

        HttpPost httpPost = new HttpPost(URI.create(uploadUrlResponse.uploadUrl));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addPart("video", new FileBody(video));
        httpPost.setEntity(multipartEntityBuilder.build());
        org.apache.http.HttpResponse response = advancedClient.execute(httpPost);

        String responseString = responseAsString(response);
        System.out.println("video: " + responseString);
        return uploadUrlResponse.videoId;
    }

    private static String responseAsString(org.apache.http.HttpResponse response) throws IOException {
        return new BufferedReader(
                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    private PhotoUploadUrlResponse photoUploadUrl(String accessToken, long groupId, List<File> photos) throws Exception {
        URI uri = new URIBuilder("https://api.ok.ru/fb.do")
                .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                .addParameter("count", String.valueOf(photos.size()))
                .addParameter("format", "json")
                .addParameter("gid", String.valueOf(groupId))
                .addParameter("method", UPLOAD_PHOTO)
                .addParameter("sig", OkAuthorizator.sig(accessToken, UPLOAD_PHOTO))
                .addParameter("access_token", accessToken)
                .build();
        HttpRequest getUploadUrlRequest = HttpRequest.newBuilder().GET()
                .uri(uri)
                .build();
        HttpResponse<String> uploadUrlResponse = client.send(getUploadUrlRequest, HttpResponse.BodyHandlers.ofString());

        JSONObject responseBodyJson = new JSONObject(uploadUrlResponse.body());
        JSONArray photoTokensJsonList = responseBodyJson.getJSONArray("photo_ids");
        String uploadUrl = responseBodyJson.getString("upload_url");
        List<String> photoTokens = IntStream.range(0, photoTokensJsonList.length())
                .boxed()
                .map(photoTokensJsonList::getString)
                .toList();

        return new PhotoUploadUrlResponse(uploadUrl, photoTokens);
    }

    private VideoUploadUrlResponse videoUploadUrl(String accessToken, long groupId, String fileName, long fileSize) throws URISyntaxException, IOException, InterruptedException {
        URI uri = new URIBuilder("https://api.ok.ru/fb.do")
                .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                .addParameter("file_name", fileName)
                .addParameter("file_size", String.valueOf(fileSize))
                .addParameter("format", "json")
                .addParameter("gid", String.valueOf(groupId))
                .addParameter("method", UPLOAD_VIDEO)
                .addParameter("sig", OkAuthorizator.sig(accessToken, UPLOAD_VIDEO))
                .addParameter("access_token", accessToken)
                .build();
        HttpRequest getUploadUrlRequest = HttpRequest.newBuilder().GET()
                .uri(uri)
                .build();
        HttpResponse<String> uploadUrlResponse = client.send(getUploadUrlRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(uploadUrlResponse.body());
        JSONObject object = new JSONObject(uploadUrlResponse.body());

        return new VideoUploadUrlResponse(
                object.getString("upload_url"),
                object.getLong("video_id")
        );
    }

    private record VideoUploadUrlResponse(String uploadUrl, long videoId) {
    }

    private record PhotoUploadUrlResponse(String uploadUrl, List<String> photoTokens) {
    }
}
