#!/usr/bin/env groovy
package com.sharedlib

class TrivyScan implements Serializable {
    def script

    TrivyScan(script) { 
        this.script = script 
    }

    def trivyScan(Map config = [:]) {

        def required = [
            "MODE",
            "TARGET",
            "SCAN_FORMAT",
            "OUTPUT_REPORT",
            "SEVERITY"
        ]

        required.each { key ->
            if (!config[key] || config[key].toString().trim() == "") {
                script.error "❌ TRIVY ${config.MODE?.toUpperCase()?.trim()} SCAN: Missing required parameter '${key}'"
            }
        }

        def mode         = config.MODE
        def target       = config.TARGET
        def scanFormat   = config.SCAN_FORMAT
        def outputReport = config.OUTPUT_REPORT
        def severity     = config.SEVERITY

        script.echo "⏳ Running TRIVY ${mode} SCAN for: '${target}'"

        try {
            script.sh """
                trivy "${mode}" "${target}" \
                --format "${scanFormat}" \
                --output "${outputReport}" \
                --severity "${severity}"
            """
        } catch (Exception ex) {
            script.error "❌ Trivy "${mode}" scan step failed: ${ex.message}"
        }

        script.echo "✔ Trivy ${mode} scan completed successfully. Report stored at: '${script.env.WORKSPACE}/${outputReport}'"
    }
}
