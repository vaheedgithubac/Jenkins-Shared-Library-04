#!/usr/bin/env groovy
package com.sharedlib

class GitCheckout implements Serializable {
	def script

	GitCheckout(script) { this.script = script }

	def gitCheckout(Map config = [:]) {

		def required = ["MY_GIT_URL", "MY_GIT_REPO_TYPE"]
	    required.each { key ->
	        if (!config[key] || config[key]?.toString().trim() == "") {
	            error "❌ GIT: Missing required parameter '${key}'"
	        }
	    }

	    def my_git_repo_type = config.MY_GIT_REPO_TYPE.toLowerCase().trim()
	    def my_git_url       = config.MY_GIT_URL.trim()
	    def my_git_branch    = config.MY_GIT_BRANCH ?: 'main'
	    def my_git_credentials_id = config.MY_GIT_CREDENTIALS_ID ?: null

	    if (!(my_git_repo_type in ['private', 'public'])) {
	        error "❌ MY_GIT_REPO_TYPE must be 'public' or 'private'. Current: '${my_git_repo_type}'"
	    }

	    if (my_git_repo_type == "private") {
	        if (!my_git_credentials_id || my_git_credentials_id.trim().toLowerCase() == "null") {
	            error "❌ MY_GIT_CREDENTIALS_ID is required for private repositories."
	        } else { echo "⚡ Private repo detected, git credentials must be supplied." }
	    } else { echo "⚡ Public repo detected, git credentials not needed." }

    	script.echo "⏳ Checking out Branch:${config.MY_GIT_BRANCH} From:${config.MY_GIT_URL}..."
    	
    	// Printing Values	
    	echo "✔ MY_GIT_URL            = ${my_git_url}"
	    echo "✔ MY_GIT_REPO_TYPE      = ${my_git_repo_type}"
	    echo "✔ MY_GIT_BRANCH         = ${my_git_branch}"
	    echo "✔ MY_GIT_CREDENTIALS_ID = ${my_git_credentials_id}"

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
