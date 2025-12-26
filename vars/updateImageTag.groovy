#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {
    if (!env.GIT_USER || !env.GIT_TOKEN) {
        error("❌ GIT_USER or GIT_TOKEN not set in environment")
    }

    config.GIT_USER  = env.GIT_USER
    config.GIT_TOKEN = env.GIT_TOKEN

    new UpdateImageTag(this).updateImageTag(config)
}

