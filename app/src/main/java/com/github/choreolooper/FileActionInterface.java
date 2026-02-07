package com.github.choreolooper;

/**
 * Interface for invoking file actions on a different component.
 */
public interface FileActionInterface {

    /**
     * Rename a file.
     *
     * @param targetFile Current name of the file to be modified.
     * @param newName New name to use for the file.
     */
    void rename(String targetFile, String newName);

    /**
     * Delete a file.
     *
     * @param targetFile Name of the file to be deleted.
     */
    void delete(String targetFile);

    /**
     * Load the contents of a file.
     *
     * @param targetFile Name of the file to be loaded.
     */
    void open(String targetFile);
}
