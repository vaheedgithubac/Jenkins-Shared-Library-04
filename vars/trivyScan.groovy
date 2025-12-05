#!/usr/bin/env groovy
import com.sharedlib.TrivyScan

def call(Map config = [:]) {

    def required = [
        "MODE",
        "TARGET",
        "SCAN_FORMAT",
        "OUTPUT_FORMAT",
        "PROJECT_NAME",
        "COMPONENT",
        "MY_GIT_LATEST_COMMIT_ID"
    ]

    required.each { key ->
        if (!config[key] || config[key].trim() == "") {
            error "❌ TRIVY ${config.MODE.toUpperCase()?.trim()} SCAN: Missing required parameter '${key}'"
        }
    }

    def mode                 = config.MODE
    def target               = config.TARGET
    def scan_format          = config.SCAN_FORMAT
    def output_format        = config.OUTPUT_FORMAT
    def project_name         = config.PROJECT_NAME
    def component            = config.COMPONENT
    def git_latest_commit_id = config.MY_GIT_LATEST_COMMIT_ID
    
    def ext = [
        "table": "txt",
        "json" : "json",
        "sarif": "sarif",
        "xml"  : "xml",
        "yaml" : "yaml"
    ][scan_format] ?: scan_format  // fallback to 'output_report_format' if unknown
    
    def output_report = ""
    def outDir        = "trivy-reports"
    sh "mkdir -p ${outDir}"

    if (mode.toLowerCase()?.trim() == "fs" ) {
        output_report = "${outDir}/${project_name}-${component}-${mode}-${git_latest_commit_id}.${ext}"   // trivy-reports/expense-backend-fs-7drt46y.html
    }
    else if (mode.toLowerCase()?.trim() == "image") {
        output_report = "${outDir}/${project_name}-${component}-${mode}-${git_latest_commit_id}.${ext}"   // trivy-reports/expense-backend-image-7drt46y.html
    }
    else {
        error "❌ Invalid mode: Choose 'fs' or 'image'"
    }

    echo "🛡 Running Trivy scan"
    echo "📄 Output: '${output_report}'"
    echo "🎯 Target: '${target}'"

    
    return new TrivyScan(this).trivyScan(
        MODE:          mode,
        TARGET:        target,
        SCAN_FORMAT:   scan_format,
        OUTPUT_REPORT: output_report,
        SEVERITY: "MEDIUM,HIGH,CRITICAL"
    )
}
