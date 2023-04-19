package polis.dataCheck.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polis.dataCheck.domain.Attachment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static polis.dataCheck.api.LoggingUtils.parseResponse;
import static polis.dataCheck.api.LoggingUtils.sendRequest;
import static polis.dataCheck.api.LoggingUtils.wrapAndLog;

public class OkClientImpl implements OKClient {
    private static final String POST_MEDIA_TOPIC = "mediatopic.post";
    private static final String UPLOAD_PHOTO = "photosV2.getUploadUrl";
    private static final String UPLOAD_VIDEO = "video.getUploadUrl";
    private static final Logger logger = LoggerFactory.getLogger(OkAuthorizator.class);
    private final Integer clientResponseTimeout = 5;
    private final RequestConfig config;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public OkClientImpl() {
         config = RequestConfig.custom()
                .setConnectTimeout(clientResponseTimeout * 1000)
                .setConnectionRequestTimeout(clientResponseTimeout * 1000)
                .setSocketTimeout(clientResponseTimeout * 1000).build();
    }

    public void postMediaTopic(String accessToken, long groupId, Attachment attachment)
            throws URISyntaxException, IOException, OkApiException {
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
        HttpResponse<String> response = sendRequest(client, request, logger);
        Matcher matcher = Pattern.compile("\"(\\d+)\"").matcher(response.body());
        if (matcher.matches()) {
            String postId = matcher.group(1);
            logger.info("Posted post %s to group %d".formatted(postId, groupId));
        } else {
            parseResponse(response, logger);
        }
    }

    public List<String> uploadPhotos(String accessToken, long groupId, List<File> photos)
            throws URISyntaxException, IOException, OkApiException {
        PhotoUploadUrlResponse uploadUrlResponse = photoUploadUrl(accessToken, groupId, photos);

        HttpPost httpPost = new HttpPost(URI.create(uploadUrlResponse.uploadUrl));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        for (int i = 0; i < photos.size(); i++) {
            File photo = photos.get(i);
            multipartEntityBuilder.addPart("pic" + (i + 1), new FileBody(photo));
        }
        httpPost.setEntity(multipartEntityBuilder.build());

        try (CloseableHttpClient advancedClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            org.apache.http.HttpResponse response = sendRequest(advancedClient, httpPost, logger);
            JSONObject responseJson = parseResponse(response, logger);

            try {
                JSONObject photoIds = responseJson.getJSONObject("photos");
                List<String> result = new ArrayList<>(photos.size());
                for (int i = 0; i < photos.size(); i++) {
                    String photoToken = uploadUrlResponse.photoTokens.get(i);
                    String photoId = photoIds.getJSONObject(photoToken).getString("token");
                    result.add(photoId);
                }
                return result;
            } catch (JSONException e) {
                throw wrapAndLog(e, "", "", logger);
            } catch (IndexOutOfBoundsException e) {
                throw new OkApiException(e);
            }
        }
    }

    public long uploadVideo(String accessToken, long groupId, File video)
            throws URISyntaxException, IOException, OkApiException {
        VideoUploadUrlResponse uploadUrlResponse = videoUploadUrl(accessToken, groupId, video.getName(),
                video.length());

        HttpPost httpPost = new HttpPost(URI.create(uploadUrlResponse.uploadUrl));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addPart("video", new FileBody(video));
        httpPost.setEntity(multipartEntityBuilder.build());

        try (CloseableHttpClient advancedClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
            sendRequest(advancedClient, httpPost, logger);
        }

        return uploadUrlResponse.videoId;
    }

    private PhotoUploadUrlResponse photoUploadUrl(String accessToken, long groupId, List<File> photos)
            throws URISyntaxException, IOException, OkApiException {
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
        HttpResponse<String> uploadUrlResponse = sendRequest(client, getUploadUrlRequest, logger);
        JSONObject responseBodyJson = parseResponse(uploadUrlResponse, logger);

        try {
            JSONArray photoTokensJsonList = responseBodyJson.getJSONArray("photo_ids");
            String uploadUrl = responseBodyJson.getString("upload_url");
            List<String> photoTokens = IntStream.range(0, photoTokensJsonList.length())
                    .boxed()
                    .map(photoTokensJsonList::getString)
                    .toList();
            return new PhotoUploadUrlResponse(uploadUrl, photoTokens);
        } catch (JSONException e) {
            throw wrapAndLog(e, uploadUrlResponse.toString(), uploadUrlResponse.body(), logger);
        }
    }

    private VideoUploadUrlResponse videoUploadUrl(String accessToken, long groupId, String fileName, long fileSize)
            throws URISyntaxException, IOException, OkApiException {
        URI uri = new URIBuilder("https://api.ok.ru/fb.do")
                .addParameter("application_key", OkAppProperties.APPLICATION_KEY)
                .addParameter("file_name", fileName)
                .addParameter("file_size", String.valueOf(fileSize))
                .addParameter("format", "json")
                .addParameter("gid", String.valueOf(groupId))
                .addParameter("method", UPLOAD_VIDEO)
                .addParameter("sig", OkAuthorizator.sig(accessToken, UPLOAD_VIDEO))
                .addParameter("access_token", accessToken)
                .addParameter("post_form", "true")
                .build();
        HttpRequest getUploadUrlRequest = HttpRequest.newBuilder().GET()
                .uri(uri)
                .build();
        HttpResponse<String> uploadUrlResponse = sendRequest(client, getUploadUrlRequest, logger);
        JSONObject object = parseResponse(uploadUrlResponse, logger);

        try {
            return new VideoUploadUrlResponse(
                    object.getString("upload_url"),
                    object.getLong("video_id")
            );
        } catch (JSONException e) {
            throw wrapAndLog(e, uploadUrlResponse.toString(), uploadUrlResponse.body(), logger);
        }
    }

    private record VideoUploadUrlResponse(String uploadUrl, long videoId) {
    }

    private record PhotoUploadUrlResponse(String uploadUrl, List<String> photoTokens) {
    }

}
