#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {

    def required = [
        "GIT_REPO_NAME",
        "GIT_BRANCH_NAME",
        "MY_GIT_LATEST_COMMIT_ID",
        "VERSION_CONTROL_SYSTEM",
        "GIT_DEPLOY_HTTPS_CREDS"
    ]

    required.each { key ->
        if (!config[key]?.toString()?.trim()) {
            script.error("❌ UPDATE IMAGE TAG: Missing required parameter '${key}' (src/com/sharedlib)")
        }
    }

    def updater = new UpdateImageTag(this)

    def deploymentFilePath = "${env.WORKSPACE}/${config.DEPLOYMENT_FILE?.trim()}"   
    
    def customConfig = [:]

    customConfig.GIT_REPO_NAME           = config.GIT_REPO_NAME
    customConfig.GIT_BRANCH_NAME         = config.GIT_BRANCH_NAME
    customConfig.MY_GIT_LATEST_COMMIT_ID = config.MY_GIT_LATEST_COMMIT_ID
    customConfig.VERSION_CONTROL_SYSTEM  = config.VERSION_CONTROL_SYSTEM
    customConfig.GIT_DEPLOY_HTTPS_CREDS  = config.GIT_DEPLOY_HTTPS_CREDS

    
    if (config.DEPLOYMENT_FILE) {
        
        if (fileExists(deploymentFilePath)) {
            
            echo "✅ Found given Deployment file:${config.DEPLOYMENT_FILE} at: ${deploymentFilePath}" 

            if (!config.TAGGED_DOCKER_IMAGE && !config.TAGGED_ECR_IMAGE) { error "Neither TAGGED_DOCKER_IMAGE nor TAGGED_ECR_IMAGE was provided..."}

            if (config.TAGGED_DOCKER_IMAGE) {

                def dockerConfig = customConfig.clone()

                dockerConfig.TAGGED_IMAGE     = config.TAGGED_DOCKER_IMAGE
                dockerConfig.DEPLOYMENT_FILE  = config.DEPLOYMENT_FILE

                echo "dockerConfig.TAGGED_IMAGE = ${dockerConfig.TAGGED_IMAGE}"
                updater.updateImageTag(dockerConfig)
            }

            if (config.TAGGED_ECR_IMAGE) {

                def ecrConfig = customConfig.clone()

                ecrConfig.TAGGED_IMAGE            = config.TAGGED_ECR_IMAGE
                ecrConfig.DEPLOYMENT_FILE         = config.DEPLOYMENT_FILE

                echo "ecrConfig.TAGGED_IMAGE = ${ecrConfig.TAGGED_IMAGE}"
                updater.updateImageTag(ecrConfig)
            }

        } else { error "Not found given Deployment file: ${config.DEPLOYMENT_FILE} at: ${deploymentFilePath}"}

    } else { error "No Deployment file provided" }


    def helmValuesFilePath = "${env.WORKSPACE}/${config.HELM_VALUES_FILE?.trim()}"
    if (config.HELM_VALUES_FILE) {

        if (fileExists(helmValuesFilePath)) {

            echo "✅ Found Helm file:${config.HELM_VALUES_FILE} at: ${helmValuesFilePath}" 

            if (!config.HELM_IMAGE_VERSION_KEY) { error "HELM_IMAGE_VERSION_KEY was not provided..."}

            def helmConfig = customConfig.clone()

            helmConfig.HELM_VALUES_FILE       = config.HELM_VALUES_FILE
            helmConfig.HELM_IMAGE_VERSION_KEY = config.HELM_IMAGE_VERSION_KEY

            echo "helmConfig.HELM_IMAGE_VERSION_KEY: ${helmConfig.HELM_IMAGE_VERSION_KEY}"
            updater.updateImageTag(helmConfig)

        } else { error "Not found given HELM values file: ${config.HELM_VALUES_FILE} at: ${helmValuesFilePath}" }

    } else { error "No HELM values file provided" }

    return results
}
