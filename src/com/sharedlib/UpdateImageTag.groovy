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
            "GIT_REPO_NAME",
            "GIT_BRANCH_NAME",
            "MY_GIT_LATEST_COMMIT_ID",
            "VERSION_CONTROL_SYSTEM",
            "GIT_DEPLOY_HTTPS_CREDS"
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
        
        def searchImage  = "${imageName}"
        def replaceImage = "${imageName}:${config.MY_GIT_LATEST_COMMIT_ID}"

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

        // Call git commit and push with credentials ID
        gitCommitAndPush(
            FILES: filesToCommit,
            GIT_REPO_NAME: config.GIT_REPO_NAME,
            GIT_BRANCH_NAME: config.GIT_BRANCH_NAME,
            MY_GIT_LATEST_COMMIT_ID: config.MY_GIT_LATEST_COMMIT_ID,
            VERSION_CONTROL_SYSTEM: config.VERSION_CONTROL_SYSTEM,
            GIT_DEPLOY_HTTPS_CREDS: config.GIT_DEPLOY_HTTPS_CREDS
        )
    }

    // Check whether 'Files' are there to commit 
    private void gitCommitAndPush(Map config = [:]) {
        if (!config.FILES || config.FILES.isEmpty()) {
            script.echo "ℹ️ No files to commit. Skipping git commit & push."
            return
        }

        def required = [
            "GIT_REPO_NAME",
            "GIT_BRANCH_NAME",
            "MY_GIT_LATEST_COMMIT_ID",
            "VERSION_CONTROL_SYSTEM",
            "GIT_DEPLOY_HTTPS_CREDS"  
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

        // Configure git user
        script.sh "git config user.name 'jenkins'"
        script.sh "git config user.email 'jenkins@company.com'"

        // Stage files
        script.sh "git add ${config.FILES.join(' ')}"

        // Commit changes
        def commitStatus = script.sh(
            script: "git commit -m \"Update image tag to ${config.MY_GIT_LATEST_COMMIT_ID}\"",
            returnStatus: true
        )
        
        if (commitStatus != 0) {
            script.echo "ℹ️ Nothing to commit."
            return
        } else {
            script.echo "✅ Changes committed successfully."
        }

        
        // Push safely using Jenkins credentials ID
        def pushStatus = script.withCredentials([
            script.usernamePassword(
                credentialsId: config.GIT_DEPLOY_HTTPS_CREDS,
                usernameVariable: 'GIT_USER_SAFE',
                passwordVariable: 'GIT_TOKEN_SAFE'
            )
        ]) {
            // def safeGitUrl = "https://${script.env.GIT_USER_SAFE}:${script.env.GIT_TOKEN_SAFE}@${vcsHost}/${script.env.GIT_USER_SAFE}/${config.GIT_REPO_NAME}.git"
            // replace '$' with '\$' to tell groovy not to interpret variable but to pass it as is to shell to interpret by shell(shell sees '$GIT_USER_SAFE', groovy sees 'env.${GIT_USER_SAFE}')
            def safeGitUrl = "https://\$GIT_USER_SAFE:\$GIT_TOKEN_SAFE@${vcsHost}/\$GIT_USER_SAFE/${config.GIT_REPO_NAME}.git"
            // Passed to shell --> "https://$GIT_USER_SAFE:$GIT_TOKEN_SAFE@github.com/$GIT_USER_SAFE/reponame.git"
            return script.sh(
                script: "git push ${safeGitUrl} HEAD:${config.GIT_BRANCH_NAME}",
                returnStatus: true
            )
        }

        if (pushStatus != 0) {
            script.error "⚠️ Git push failed or nothing to push. Check credentials or remote branch permissions."
        } else {
            script.echo "✅ Image tag pushed successfully for files: '${config.FILES.join(', ')}' with latest tag: '${config.MY_GIT_LATEST_COMMIT_ID}'"
            script.echo """
            📄 Pushed Tag Details:
                Files : '${config.FILES.join(', ')}'
                Latest Tag: '${config.MY_GIT_LATEST_COMMIT_ID}'
            """
        }
    }
}
