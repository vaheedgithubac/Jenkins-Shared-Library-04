#!/usr/bin/env groovy
import com.sharedlib.DockerPush

def call(Map config = [:]) {

    def required = ["DOCKER_IMAGE", "DOCKER_HUB_CREDENTIALS_ID", "DOCKER_REPO_URI"]
    required.each { key ->
        if (!config[key]) {
            error "❌ DOCKER REGISTRY: Missing required parameter '${key}'"
        }
    }
   
    return new DockerPush(this).dockerPush(
        DOCKER_IMAGE:              config.DOCKER_IMAGE,
        DOCKER_HUB_CREDENTIALS_ID: config.DOCKER_HUB_CREDENTIALS_ID,
        DOCKER_REPO_URI:           config.DOCKER_REPO_URI ?: "docker.io"
    )
}
