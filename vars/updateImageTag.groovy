#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {

    def results = []
    def updater = new UpdateImageTag(this)

    if (!config.DEPLOYMENT_FILE && !config.HELM_VALUES_FILE) { error("Neither DEPLOYMENT_FILE nor HELM_VALUES_FILE provided...") }

    def deploymentFilePath = "${env.WORKSPACE}/${config.DEPLOYMENT_FILE?.trim()}"   
    def helmValuesFilePath = "${env.WORKSPACE}/${config.HELM_VALUES_FILE?.trim()}"

    
    if (fileExists(deploymentFilePath)) { 
        echo "✅ Deployment file:${config.DEPLOYMENT_FILE} found at:${deploymentFilePath}" 

        if (!config.TAGGED_DOCKER_IMAGE && !config.TAGGED_ECR_IMAGE) { error "Neither TAGGED_DOCKER_IMAGE nor TAGGED_ECR_IMAGE was provided..."}
        
        if (config.TAGGED_DOCKER_IMAGE) {
            def dockerConfig = config.clone()
            dockerConfig.TAGGED_IMAGE = config.TAGGED_DOCKER_IMAGE
            results << updater.updateImageTag(dockerConfig)
        }

        if (config.TAGGED_ECR_IMAGE) {
            def ecrConfig = config.clone()
            ecrConfig.TAGGED_IMAGE = config.TAGGED_ECR_IMAGE
            results << updater.updateImageTag(ecrConfig)
        }
    }

    if (fileExists(helmValuesFilePath)) { 
        echo "✅ Helm file:${config.HELM_VALUES_FILE} found at:${helmValuesFilePath}" 

        if (!config.HELM_IMAGE_VERSION_KEY) { error "HELM_IMAGE_VERSION_KEY was not provided..."}

        def helmConfig = config.clone()
        results << updater.updateImageTag(helmConfig)
    }
    return results
}
