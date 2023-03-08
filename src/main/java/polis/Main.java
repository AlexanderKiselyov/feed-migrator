package polis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        OkAuthorization client = new OkAuthorization();
        Scanner in = new Scanner(System.in);
        System.out.println("Please follow the url: ");
        System.out.println(client.formAuthorizationUrl());
        System.out.println("Write code to code.txt and print anything: "); //У меня не получилось сканером нормально прочитать из консоли скопированный код :)
        String ignored = in.nextLine();
        String code = Files.readString(Path.of("code.txt")).trim();
        System.out.println("Retrieving token: ");
        OkAuthorization.TokenPair token = client.getToken(code);
        System.out.println(token);
    }

}
