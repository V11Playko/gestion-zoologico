package com.playko.zoologico.dto.request;

public class PdfFile {
    private final byte[] content;
    private final String creatorEmail;

    public PdfFile(byte[] content, String creatorEmail) {
        this.content = content;
        this.creatorEmail = creatorEmail;
    }

    public byte[] getContent() {
        return content;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }
}
