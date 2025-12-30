#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {

    def results = []
    def updater = new UpdateImageTag(this)

    if (config.TAGGED_DOCKER_IMAGE) {
        def dockerConfig = config.clone()
        dockerConfig.TAGGED_IMAGE = env.TAGGED_DOCKER_IMAGE
        results << updater.updateImageTag(dockerConfig)
    }

    if (config.TAGGED_ECR_IMAGE) {
        def ecrConfig = config.clone()
        ecrConfig.TAGGED_IMAGE = env.TAGGED_ECR_IMAGE
        results << updater.updateImageTag(ecrConfig)
    }

    if (results.isEmpty()) {
        error "Neither TAGGED_DOCKER_IMAGE nor TAGGED_ECR_IMAGE was provided"
    }

    return results
}


