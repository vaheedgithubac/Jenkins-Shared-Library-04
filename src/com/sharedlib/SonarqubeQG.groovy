#!/usr/bin/env groovy
package com.sharedlib

class SonarqubeQG implements Serializable {
    def script

    SonarqubeQG(script) { this.script = script }

    def sonarqubeQG(Map config = [:]) {
        // Default timeout in minutes
        def timeoutMinutes = (config.TIMEOUT_MINUTES ?: 5).toInteger()

        script.timeout(time: timeoutMinutes, unit: "MINUTES") {
            def qg = script.waitForQualityGate(abortPipeline: false)

            script.echo "🔹 SonarQube Quality Gate status: ${qg.status}"

            /* Uncomment the following if you want to fail the pipeline on Quality Gate failure
               if (qg.status != 'OK') {
                   script.error "❌ Pipeline aborted due to Quality Gate failure: ${qg.status}"
              } */
        }
    }
}
