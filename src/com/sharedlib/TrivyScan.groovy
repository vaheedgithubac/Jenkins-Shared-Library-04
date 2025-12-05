#!/usr/bin/env groovy
package com.sharedlib

class TrivyScan implements Serializable {
	def script

	TrivyScan(script) { this.script = script }

	def trivyScan(Map config = [:]) {

		def required = ["MODE", "TARGET", "SCAN_FORMAT", "OUTPUT_REPORT", "SEVERITY"]
    	required.each { key ->
        	if (!config[key] || config[key].trim() == "") {
            	error "❌ TRIVY: Missing required parameter '${key}'"
        	}
    	}

    	def mode          = config.MODE
    	def target        = config.TARGET
    	def scan_format   = config.SCAN_FORMAT
    	def output_report = config.OUTPUT_REPORT
    	def severity      = config.SEVERITY

    	script.echo "⏳ Running TRIVY ${mode} SCAN for : '${target}'"

    	try { script.sh """
            		trivy ${mode} ${target} \
            		--format ${scan_format} \
            		--output ${output_report} \
            		--severity ${severity}  
    			"""
        } catch (Exception ex) { error "❌ Jacoco Maven step failed: ${ex.message}" }

    	echo "✔ Trivy scan completed successfully. Report stored at: '${env.WORKSPACE}/${output_report}'"

    	// sh 'mvn org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent'
	}
}
