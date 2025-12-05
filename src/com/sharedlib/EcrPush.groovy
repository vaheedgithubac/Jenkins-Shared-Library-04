#!/usr/bin/env groovy
package com.sharedlib

class EcrPush implements Serializable {
	def script

	EcrPush(script) { this.script = script }

	def ecrPush(Map config = [:]) {

		def required = ["DOCKER_IMAGE", "ECR_REPO_URI", "REGION", "AWS_CREDENTIALS_ID"]
    	required.each { key ->
        	if (!config[key] || config[key]?.trim() == "") {
            	script.error "❌ ECR PUSH: Missing required parameter '${key}'"
        	}
    	}

    	def dockerImage   = config.DOCKER_IMAGE
	    def ecrRepoUri    = config.ECR_REPO_URI
	    def region        = config.REGION ?: "ap-south-1"
	    def credentialsId = config.AWS_CREDENTIALS_ID

	    script.withAWS(credentials: credentialsId, region: "${region}") {  // Plugin: AWS steps
	        script.sh """
	            echo "🔖 Tagging Docker Image"
	            docker tag ${dockerImage} ${ecrRepoUri}/${dockerImage}
	            # docker tag ${projectName}-${component}:${imageTag} ${ecrRepoUri}/${projectName}-${component}:${imageTag}

	            echo "🔐 Logging into ECR"
	            set +x
	            aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrRepoUri}
	            set -x

	            echo "🚀 Pushing Docker Image to ECR Repo"
	            docker push ${ecrRepoUri}/${dockerImage}
	            # docker push ${ecrRepoUri}/${projectName}-${component}:${imageTag}
	            
	            echo "✔ Pushed Docker Image to ECR Successfully"

	            # Logout and final confirmation
	            echo "🔐 Logging out from ECR"
	            docker logout ${ecrRepoUri}
	            echo "✔ Logged out from ECR Successfully"
	        """
	    } 
	}
}

