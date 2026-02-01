package com.github.choreolooper;

public interface FileActionInterface {
    void rename(String targetFile, String newName);
    void delete(String targetFile);
    void open(String targetFile);
}
