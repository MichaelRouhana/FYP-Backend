package com.example.FYP.Api.Listener;

import jakarta.persistence.PostLoad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetListener {



    @PostLoad
    public void onPostLoad( ) {
    }
}
