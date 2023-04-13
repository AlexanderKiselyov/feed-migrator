package polis.ok.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public final class DocumentMedia extends Media implements Serializable {
    @JsonProperty("list")
    public Collection<Document> documents;

    public DocumentMedia(Collection<Document> documents) {
        super("document");
        this.documents = documents;
    }

    public DocumentMedia(int documentsCount) {
        this(new ArrayList<>(documentsCount));
    }

    public void addDocument(Document document) {
        documents.add(document);
    }
}
