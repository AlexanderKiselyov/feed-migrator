package polis.ok.api;

import polis.ok.domain.Attachment;

import java.io.File;
import java.util.List;

//TODO exceptions!
public interface OKClient {

    void postMediaTopic(String accessToken, long groupId, Attachment attachment) throws Exception;

    long uploadVideo(String accessToken, long groupId, File video) throws Exception;

    List<String> uploadPhotos(String accessToken, long groupId, List<File> photos) throws Exception;
}
