#!/usr/bin/env groovy
import com.sharedlib.EcrPush

def call(Map config = [:]) {

   def required = ["DOCKER_IMAGE", "ECR_REPO_URI", "REGION", "AWS_CREDENTIALS_ID"]
   required.each { key ->
      if (!config[key] || config[key]?.trim() == "") {
            error "❌ TRIVY: Missing required parameter '${key}'"
      }
   }
   
    return new EcrPush(this).ecrPush(
        DOCKER_IMAGE:       config.DOCKER_IMAGE,
        ECR_REPO_URI:       config.ECR_REPO_URI,
        REGION:             config.REGION,
        AWS_CREDENTIALS_ID: config.AWS_CREDENTIALS_ID
    )
}
