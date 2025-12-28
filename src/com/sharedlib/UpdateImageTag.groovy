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

        // Required parameters
        def required = [
            "VERSION_CONTROL_SYSTEM",
            "GIT_REPO_NAME",
            "GIT_BRANCH_NAME",
            "GIT_DEPLOY_HTTPS_CREDS",
            "MY_GIT_LATEST_COMMIT_ID"
        ]

        required.each { key ->
            if (!config[key]?.toString()?.trim()) {
                script.error("❌ GIT COMMIT: Missing required parameter '${key}'")
            }
        }

        def vcsHost
        def vcs = config.VERSION_CONTROL_SYSTEM?.trim()?.toLowerCase() ?: "github"
    
        switch (vcs) {
            case "github": vcsHost = "github.com"; break
            case "gitlab": vcsHost = "gitlab.com"; break
            default: script.error("❌ Unsupported VERSION_CONTROL_SYSTEM: ${config.VERSION_CONTROL_SYSTEM}")
        }

        // Stage files
        script.sh(["git", "config", "user.name", "jenkins"])
        script.sh(["git", "config", "user.email", "jenkins@company.com"])
        script.sh(["git", "add"] + config.FILES)

        // Commit changes if any
        def commitStatus = script.sh(
                script: ['git', 'commit', '-m', "Update image tag to '${config.MY_GIT_LATEST_COMMIT_ID}'"],
            returnStatus: true
        )

        if (commitStatus != 0) {
            script.echo "ℹ️ Nothing to commit. Skipping commit."
        } else {
            script.echo "✅ Changes committed successfully."
        }

        // Push safely using Jenkins credentials
        def pushStatus = script.withCredentials([
            script.usernamePassword(
                credentialsId: config.GIT_DEPLOY_HTTPS_CREDS,
                usernameVariable: 'GIT_USER_SAFE',
                passwordVariable: 'GIT_TOKEN_SAFE'
            )
        ]) {
            def safeGitUrl = "https://${env.GIT_USER_SAFE}:${env.GIT_TOKEN_SAFE}@${vcsHost}/${env.GIT_USER_SAFE}/${config.GIT_REPO_NAME}.git"
            return script.sh(
                script: ['git', 'push', safeGitUrl, "HEAD:${config.GIT_BRANCH_NAME}"],
                returnStatus: true
            )
        }

        if (pushStatus != 0) {
            script.echo "⚠️ Git push failed or nothing to push. Check credentials or remote branch status."
        } else {
            script.echo "✅ Image tag pushed successfully for files: ${config.FILES.join(', ')}"
        }
    }
}
