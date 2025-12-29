#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {
    new UpdateImageTag(this).updateImageTag(config)
}

