#!/usr/bin/env groovy
package com.sharedlib

class UpdateImageTag implements Serializable {

    def script

    UpdateImageTag(script) {
        this.script = script
    }

    def updateImageTag(Map config = [:]) {

        def required = [
            "DOCKER_IMAGE",
            "GIT_USER",
            "GIT_TOKEN",
            "GIT_REPO_NAME",
            "GIT_BRANCH_NAME",
            "MY_GIT_LATEST_COMMIT_ID",
            "VERSION_CONTROL_SYSTEM"
        ]

        required.each { key ->
            if (!config[key]?.toString()?.trim()) {
                script.error("❌ UPDATE IMAGE TAG: Missing required parameter '${key}'")
            }
        }

        def deploymentFile = config.DEPLOYMENT_FILE
        def helmValuesFile = config.HELM_VALUES_FILE

        def filesToCommit = []

        def fullDockerImage = config.DOCKER_IMAGE
        def imageName = fullDockerImage.trim().split(':')[0]

        def searchImage  = "${config.GIT_USER}/${imageName}"
        def replaceImage = "${config.GIT_USER}/${imageName}:${config.MY_GIT_LATEST_COMMIT_ID}"

        if (!deploymentFile && !helmValuesFile) {
            script.error("Neither DEPLOYMENT_FILE nor HELM_VALUES_FILE was provided")
        }

        if (deploymentFile?.trim()) {
            script.echo "Updating deployment file: ${deploymentFile}"
            script.sh "sed -i 's|image: ${searchImage}.*|image: ${replaceImage}|g' ${deploymentFile}"
            filesToCommit << deploymentFile
        }

        if (helmValuesFile?.trim()) {
            script.echo "Updating helm values file: ${helmValuesFile}"
            script.sh "sed -i 's|imageVersion:.*|imageVersion: ${config.MY_GIT_LATEST_COMMIT_ID}|' ${helmValuesFile}"
            filesToCommit << helmValuesFile
        }

        gitCommitAndPush(
            FILES: filesToCommit,
            GIT_USER: config.GIT_USER,
            GIT_TOKEN: config.GIT_TOKEN,
            GIT_REPO_NAME: config.GIT_REPO_NAME,
            GIT_BRANCH_NAME: config.GIT_BRANCH_NAME,
            MY_GIT_LATEST_COMMIT_ID: config.MY_GIT_LATEST_COMMIT_ID,
            VERSION_CONTROL_SYSTEM: config.VERSION_CONTROL_SYSTEM
        )
    }

    // ✅ MUST be here (class-level)
    private void gitCommitAndPush(Map config = [:]) {

        if (!config.FILES || config.FILES.isEmpty()) {
            script.echo "ℹ️ No files to commit. Skipping git commit & push."
            return
        }

        def required = [
            "GIT_USER",
            "GIT_TOKEN",
            "GIT_REPO_NAME",
            "GIT_BRANCH_NAME",
            "MY_GIT_LATEST_COMMIT_ID",
            "VERSION_CONTROL_SYSTEM"
        ]

        required.each { key ->
            if (!config[key]?.toString()?.trim()) {
                script.error("❌ GIT COMMIT: Missing required parameter '${key}'")
            }
        }

        def files = config.FILES.join(' ')

        def vcsHost
        switch (config.VERSION_CONTROL_SYSTEM?.trim()?.toLowerCase()) {
            case "github":
                vcsHost = "github.com"
                break
            case "gitlab":
                vcsHost = "gitlab.com"
                break
            default:
                script.error("❌ Unsupported VERSION_CONTROL_SYSTEM: ${config.VERSION_CONTROL_SYSTEM} expects github/gitlab only" )
        }

        script.sh """
            git config user.name "jenkins"
            git config user.email "jenkins@company.com"

            git add ${files}
            git commit -m "Update image tag to '${config.MY_GIT_LATEST_COMMIT_ID}'" || echo "Nothing to commit"
            git push https://${config.GIT_USER}:${config.GIT_TOKEN}@${vcsHost}/${config.GIT_USER}/${config.GIT_REPO_NAME}.git HEAD:${config.GIT_BRANCH_NAME}
        """

        script.echo "✅ Image tag updated successfully for files: ${files}"
    }
}
