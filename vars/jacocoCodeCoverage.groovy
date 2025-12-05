#!/usr/bin/env groovy
import com.sharedlib.JacocoCodeCoverage

def call(Map config = [:]) {

    // Validate config map
    def required = ["JACOCO_GROUPID", "JACOCO_ARTIFACT_ID", "JACOCO_VERSION", "JACOCO_GOAL"]
    required.each { key ->
        if (!config[key] || config[key].trim() == "") {
            error "❌ JACOCO: Missing required parameter '${key}'"
        }
    }

    def jacoco_groupId     = config.JACOCO_GROUPID.trim()
    def jacoco_artifactId  = config.JACOCO_ARTIFACT_ID.trim()
    def jacoco_version     = config.JACOCO_VERSION.trim()
    def jacoco_goal        = config.JACOCO_GOAL.trim()

    echo "✔ JACOCO_GROUPID     = ${jacoco_groupId}"
    echo "✔ JACOCO_ARTIFACT_ID = ${jacoco_artifactId}"
    echo "✔ JACOCO_VERSION     = ${jacoco_version}"
    echo "✔ JACOCO_GOAL        = ${jacoco_goal}"

    return new JacocoCodeCoverage(this).jacocoCodeCoverage(
        JACOCO_GROUPID:     jacoco_groupId,
        JACOCO_ARTIFACT_ID: jacoco_artifactId,
        JACOCO_VERSION:     jacoco_version,
        JACOCO_GOAL:        jacoco_goal
    )

   // The above step will return the latestCommitId from src/sharedlib/GitCheckout class
}
