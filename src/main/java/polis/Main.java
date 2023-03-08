package polis;

import polis.domain.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        //String token = authorize();
        String token = "-s-38e8-RCfP9O0vUtbMqOGvT.At4S9uU-aJ3ljy3DcNemsTc";
        long groupId = 70000001951728L;
        OKClient client = new OKClient();

        Collection<String> photoIds = client.uploadPhotos(token, groupId,
                List.of(new File("photo1.jpg"), new File("photo2.jpg"))
        );
        Attachment attachment = new Attachment(List.of(
                new PhotoMedia(photoIds.stream().map(UploadedPhoto::new).toList())
        ));

        System.out.println("Posting: ");
        client.postMediaTopic(token, groupId, attachment);
    }

    private static String authorize() throws URISyntaxException, IOException, InterruptedException {
        OkAuthorization authorizationClient = new OkAuthorization();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please follow the url: ");
        System.out.println(authorizationClient.formAuthorizationUrl());
        System.out.println("Write your code here: ");
        String code = reader.readLine();
        String token = authorizationClient.getToken(code).accessToken();
        System.out.println("Your token: " + token);
        return token;
    }

}
