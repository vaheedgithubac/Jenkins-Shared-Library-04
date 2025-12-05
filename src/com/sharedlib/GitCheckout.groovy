#!/usr/bin/env groovy
package com.sharedlib

class GitCheckout implements Serializable {
	def script

	GitCheckout(script) { this.script = script }

	def gitCheckout(Map config = [:]) {

    	script.echo "⏳ Checking out Branch:${config.MY_GIT_BRANCH} From:${config.MY_GIT_URL}..."

    	script.git(
        	url: config.MY_GIT_URL,
        	branch: config.MY_GIT_BRANCH,
        	credentialsId: config.MY_GIT_CREDENTIALS_ID
    	) 

    	// Capture Latest Commit ID
        latestCommitId = script.sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()

        script.echo "✔ Successfully checked out branch '${config.MY_GIT_BRANCH}' from '${config.MY_GIT_URL}'"
        script.echo "Latest Commit Id: ${latestCommitId}"

        return latestCommitId
	}
}

