#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {

    def results = []
    def updater = new UpdateImageTag(this)

    if (!config.DEPLOYMENT_FILE && !config.HELM_VALUES_FILE) {
            script.error("Neither DEPLOYMENT_FILE nor HELM_VALUES_FILE was provided, Please provide any one of them")
        }

    if (config.HELM_VALUES_FILE) {
        if (!config.HELM_IMAGE_VERSION_KEY) { error "HELM_IMAGE_VERSION_KEY was not provided..."}

        def helmConfig = config.clone()
        results << updater.updateImageTag(helmConfig)
    }

    if (config.DEPLOYMENT_FILE) {
        // if (!config.TAGGED_DOCKER_IMAGE && !config.TAGGED_ECR_IMAGE) { error "Neither TAGGED_DOCKER_IMAGE nor TAGGED_ECR_IMAGE was provided..."}
        
        if (!env.TAGGED_DOCKER_IMAGE && !env.TAGGED_ECR_IMAGE) { error "Neither TAGGED_DOCKER_IMAGE nor TAGGED_ECR_IMAGE was provided..."}
        
        if (env.TAGGED_DOCKER_IMAGE) {
            def dockerConfig = config.clone()
            dockerConfig.TAGGED_IMAGE = env.TAGGED_DOCKER_IMAGE
            results << updater.updateImageTag(dockerConfig)
        }

        if (env.TAGGED_ECR_IMAGE) {
            def ecrConfig = config.clone()
            ecrConfig.TAGGED_IMAGE = env.TAGGED_ECR_IMAGE
            results << updater.updateImageTag(ecrConfig)
        }
    }

    return results
}

