package polis;

import polis.domain.*;
import polis.domain.PollMedia.Answer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import static polis.domain.PollMedia.Option.ANONYMOUS_VOTING;
import static polis.domain.PollMedia.Option.SINGLE_CHOICE;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        //String token = authorize();
        String token = "-s-02M.sRBJ2bnCy0EeMyortYIO-5Ljg1zdL4iB-TDdwsaBb";
        long groupId = 70000001951728L;
        OKClient client = new OKClient();

        Collection<String> photoIds = client.uploadPhotos(token, groupId,
                List.of(new File("photo1.jpg"), new File("photo2.jpg"))
        );
        Attachment attachment = new Attachment(List.of(
                new PhotoMedia(photoIds.stream().map(UploadedPhoto::new).toList()),
                new TextMedia("Пост сделан при помощи Ondoklassniki api"),
                new LinkMedia("https://apiok.ru"),
                new PollMedia("Норм?",
                        List.of(new Answer("Да"), new Answer("Нет")),
                        List.of(SINGLE_CHOICE, ANONYMOUS_VOTING))
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
