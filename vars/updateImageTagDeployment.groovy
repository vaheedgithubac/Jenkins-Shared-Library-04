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
            script.error("❌ UPDATE IMAGE TAG DEPLOYMENT: Missing required parameter '${key}' (src/com/sharedlib)")
        }
    }

    def updater = new UpdateImageTag(this)
    def deploymentFilePath = "${env.WORKSPACE}/${config.DEPLOYMENT_FILE?.trim()}"   
    
    if (config.DEPLOYMENT_FILE) {
        if (fileExists(deploymentFilePath)) {
            echo "✅ Found given Deployment file:${config.DEPLOYMENT_FILE} at: ${deploymentFilePath}" 

            if (!config.TAGGED_DOCKER_IMAGE && !config.TAGGED_ECR_IMAGE) { error "Neither TAGGED_DOCKER_IMAGE nor TAGGED_ECR_IMAGE was provided..."}

            if (config.TAGGED_DOCKER_IMAGE) {
                echo "TAGGED_IMAGE = ${config.TAGGED_IMAGE}"
                updater.updateImageTag(dockerConfig)
            }

            if (config.TAGGED_ECR_IMAGE) {
                echo "TAGGED_IMAGE = ${config.TAGGED_IMAGE}"
                updater.updateImageTag(ecrConfig)
            }

        } else { error "Not found given Deployment file: ${config.DEPLOYMENT_FILE} at: ${deploymentFilePath}"}
    } else { error "No Deployment file provided" }
}
