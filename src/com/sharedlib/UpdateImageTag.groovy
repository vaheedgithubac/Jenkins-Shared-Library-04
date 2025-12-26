#!/usr/bin/env groovy
package com.sharedlib

class UpdateImageTag implements Serializable {

    def script

    UpdateImageTag(script) {
        this.script = script
    }

    def updateImageTag(Map config = [:]) {

        // ✅ Required parameters
        def required = ["DOCKER_IMAGE", "MY_GIT_LATEST_COMMIT_ID", "GIT_USER", "GIT_TOKEN"]
        required.each { key ->
            if (!config[key] || config[key].toString().trim() == "") {
                script.error("❌ UPDATE IMAGE TAG: Missing required parameter '${key}'")
            }
        }

        def latestImageTag  = config.MY_GIT_LATEST_COMMIT_ID
        def fullDockerImage = config.DOCKER_IMAGE      // expense-backend:65tdyd

        def gitUser  = config.GIT_USER
        def gitToken = config.GIT_TOKEN

        def deploymentFile = config.DEPLOYMENT_FILE
        def helmValuesFile = config.HELM_VALUES_FILE


        // -----------------------------
        // Extract image name and tag
        // -----------------------------
        def imageParts = fullDockerImage.split(':')
        def dockerImageWithoutTag = imageParts[0]
        def imageTag = (imageParts.size() > 1) ? imageParts[1] : "latest"

        script.echo """
        📄 Image Details:
            Full image       : ${fullDockerImage}
            Image without Tag: ${dockerImageWithoutTag}
            Tag              : ${imageTag}      
        """
        // ${Tag} and ${latestImageTag} both are same since we took latest built docker image
        def searchImage  = "${gitUser}/${dockerImageWithoutTag}"
        def replaceImage = "${gitUser}/${dockerImageWithoutTag}:${latestImageTag}"

        if (!config.DEPLOYMENT_FILE && !config.HELM_VALUES_FILE) {
            script.error "Neither DEPLOYMENT_FILE nor HELM_VALUES_FILE was provided"
        }
        
        if (config.DEPLOYMENT_FILE && config.HELM_VALUES_FILE) {
            script.error "Provide only one: DEPLOYMENT_FILE or HELM_VALUES_FILE"
        }

        if (config.DEPLOYMENT_FILE?.trim()) {  // covers: null, "", " "
            script.echo "Deployment file provided: ${config.DEPLOYMENT_FILE}"
            script.sh "sed -i 's|image: ${searchImage}.*|image: ${replaceImage}|g' ${deploymentFile}"
        } 

        if (config.HELM_VALUES_FILE?.trim()) {
            script.echo "Helm values file provided: ${config.HELM_VALUES_FILE}"
            script.sh "sed -i 's|version:.*|version: ${imageTag}|' ${helmValuesFile}"
        } 

        script.sh """
            git config user.name "jenkins"
            git config user.email "jenkins@company.com"

            git add ${deploymentFile}
            git commit -m "Update image tag to ${commitId}"
            git push https://${gitUser}:${gitToken}@github.com/ORG/REPO.git HEAD:${env.BRANCH_NAME}
        """

    } //def
}


2️⃣ Safe sed command

Replace only the container that matches the name:

sed -i "/- name: ${component}/{n;s|image:.*|image: ${replaceImage}|}" ${deploymentFile}

Explanation:

/- name: ${component}/

Finds the line with your container name (must match component parameter).

{n; s|image:.*|image: ${replaceImage}|}

n → moves to the next line (which should be the image: line)

s|image:.*|image: ${replaceImage}| → replaces that line only
