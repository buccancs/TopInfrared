package com.topdon.commons.poster;

import androidx.annotation.NonNull;

interface Poster {
    
    void enqueue(@NonNull Runnable runnable);

    void clear();
}
