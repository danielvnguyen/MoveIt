package com.danielvnguyen.moveit.model.gallery;

import com.google.firebase.storage.StorageReference;

public class ImageData {
    private final StorageReference reference;
    private final long creationTimeMillis;

    public ImageData(StorageReference reference, long creationTimeMillis) {
        this.reference = reference;
        this.creationTimeMillis = creationTimeMillis;
    }

    public StorageReference getReference() {
        return reference;
    }

    public long getCreationTimeMillis() {
        return creationTimeMillis;
    }
}
