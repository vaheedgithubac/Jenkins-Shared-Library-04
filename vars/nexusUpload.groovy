#!/usr/bin/env groovy
import com.sharedlib.NexusUpload

def call(Map config = [:]) {
    return new NexusUpload(this).nexusUpload(config)
}
