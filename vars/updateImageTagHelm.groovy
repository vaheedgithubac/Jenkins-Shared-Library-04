#!/usr/bin/env groovy
import com.sharedlib.UpdateImageTag

def call(Map config = [:]) {

    def required = [
        "HELM_VALUES_FILE",
        "HELM_IMAGE_VERSION_KEY",
        "GIT_REPO_NAME",
        "GIT_BRANCH_NAME",
        "MY_GIT_LATEST_COMMIT_ID",
        "VERSION_CONTROL_SYSTEM",
        "GIT_DEPLOY_HTTPS_CREDS"
    ]

    required.each { key ->
        if (!config[key]?.toString()?.trim()) {
            script.error("❌ UPDATE IMAGE TAG HELM: Missing required parameter '${key}' (vars/)")
        }
    }

    def updater = new UpdateImageTag(this)
    def helmValuesFilePath = "${env.WORKSPACE}/${config.HELM_VALUES_FILE?.trim()}"   
    
    if (fileExists(helmValuesFilePath)) {
        echo "✅ Found Helm file:${config.HELM_VALUES_FILE} at: ${helmValuesFilePath}" 

        echo "HELM_IMAGE_VERSION_KEY: '${config.HELM_IMAGE_VERSION_KEY}'...(vars/)"
        updater.updateImageTag(config)

    } else { error "Not found given HELM values file: ${config.HELM_VALUES_FILE} at: ${helmValuesFilePath}" }   
}
