#!/usr/bin/env groovy
package com.sharedlib

class GitCheckout implements Serializable {
	def script

	GitCheckout(script) { this.script = script }

	def gitCheckout(Map config = [:]) {

		def required = ["MY_GIT_URL", "MY_GIT_REPO_TYPE"]
	    required.each { key ->
	        if (!config[key] || config[key]?.toString().trim() == "") {
	            script.error "❌ GIT: Missing required parameter '${key}'"
	        }
	    }

	    def my_git_repo_type = config.MY_GIT_REPO_TYPE.toLowerCase().trim()
	    def my_git_url       = config.MY_GIT_URL.trim()
	    def my_git_branch    = config.MY_GIT_BRANCH ?: "main"
	    def my_git_credentials_id = config.MY_GIT_CREDENTIALS_ID ?: null

	    if (!(my_git_repo_type in ['private', 'public'])) {
	        script.error "❌ MY_GIT_REPO_TYPE must be 'public' or 'private'. Current: '${my_git_repo_type}'"
	    }

	    if (my_git_repo_type == "private") {
			script.echo "⚡ Private repo detected, Checking for git credentials."
	        if (!my_git_credentials_id || my_git_credentials_id?.trim().toLowerCase() == "null") {
	            script.error "❌ MY_GIT_CREDENTIALS_ID is required for private repositories."
	        } else { script.echo "⚡ Private repo detected, git credentials are supplied." }
	    } else { script.echo "⚡ Public repo detected, git credentials not needed." }

    	script.echo "⏳ Checking out Branch:${my_git_branch} From:${my_git_url}..."
    	
    	// Printing Values	
    	script.echo "✔ MY_GIT_URL            = ${my_git_url}"
	    script.echo "✔ MY_GIT_REPO_TYPE      = ${my_git_repo_type}"
	    script.echo "✔ MY_GIT_BRANCH         = ${my_git_branch}"
	    script.echo "✔ MY_GIT_CREDENTIALS_ID = ${my_git_credentials_id}"

    	script.git(
        	url: my_git_url,
        	branch: my_git_branch,
        	credentialsId: my_git_credentials_id
    	) 

    	// Capture Latest Commit ID
        def latestCommitId = script.sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()

        script.echo "✔ Successfully checked out branch '${my_git_branch}' from '${my_git_url}'"
        script.echo "Latest Commit Id: ${latestCommitId}"

        return latestCommitId
	}
}
