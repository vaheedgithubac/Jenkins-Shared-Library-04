#!/usr/bin/env groovy
package com.sharedlib

class SonarqubeScan implements Serializable {
    def script

    SonarqubeScan(script) { this.script = script }

    def sonarqubeScan(Map config = [:]) {

        def required = [ "SONARQUBE_SERVER", "SONAR_SCANNER_NAME", "PROJECT_NAME", "PROJECT_KEY" ]
        required.each { key ->
            if (!config[key] || config[key]?.toString().trim() == "") {
                script.error "❌ SONARQUBE: Missing required parameter '${key}'"
            }
        }
           
        // Validate required parameters
        def sources = config.sources ?: "."             

        def sonarqubeServer  = config.SONARQUBE_SERVER
        def sonarScannerName = config.SONAR_SCANNER_NAME
        def projectName  = config.PROJECT_NAME
        def projectKey   = config.PROJECT_KEY
        
        script.echo "🔹 SonarQube Server: ${sonarqubeServer}"
        script.echo "🔹 Sonar Scanner Name : ${sonarScannerName}"
        script.echo "🔹 Project Name : ${projectName}"
        script.echo "🔹 Project Key : ${projectKey}"

        script.withSonarQubeEnv(sonarqubeServer) {
            script.sh """
                ${sonarScannerName}/bin/sonar-scanner \
                -Dsonar.projectName="${projectName}" \
                -Dsonar.projectKey="${projectKey}" -X
            """
        }
        script.echo "✔ Sonarqube Scan completed Successfully"
    }
}
