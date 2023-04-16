package polis.ok;

import polis.ok.api.OKClient;
import polis.ok.api.OkAuthorizator;
import polis.ok.api.OkClientImpl;
import polis.ok.domain.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import static polis.ok.domain.PollMedia.Option.ANONYMOUS_VOTING;
import static polis.ok.domain.PollMedia.Option.SINGLE_CHOICE;

public class ConsoleClient {

    public static void main(String[] args) throws Exception {
        //String token = authorize();
        String token = "-s-02M.sRBJ2bnCy0EeMyortYIO-5Ljg1zdL4iB-TDdwsaBb";
        long groupId = 70000001951728L;
        OKClient client = new OkClientImpl();

        Attachment attachment = new Attachment(List.of(
                new TextMedia("Пост сделан при помощи Ondoklassniki api"),
                new LinkMedia("https://apiok.ru")
        ));

        System.out.println("Posting: ");
        client.postMediaTopic(token, groupId, attachment);
    }

    private static String authorize() throws Exception {
        OkAuthorizator authorizationClient = new OkAuthorizator();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please follow the url: " + OkAuthorizator.formAuthorizationUrl());
        System.out.println("Write your code here: ");
        String code = reader.readLine();
        String token = authorizationClient.getToken(code).accessToken();
        System.out.println("Your token: " + token);
        return token;
    }

}
