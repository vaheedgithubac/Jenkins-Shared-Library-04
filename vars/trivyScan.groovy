#!/usr/bin/env groovy
import com.sharedlib.TrivyScan

def call(Map config = [:]) {

    //----------------------------------------------------
    // Required parameter validation
    //----------------------------------------------------
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
        if (!config[key] || config[key].toString().trim() == "") {
            error "❌ TRIVY ${config.MODE?.toUpperCase()?.trim()} SCAN: Missing required parameter '${key}'"
        }
    }

    //----------------------------------------------------
    // Extract configuration
    //----------------------------------------------------
    def mode              = config.MODE.toLowerCase()
    def target            = config.TARGET
    def scanFormat        = config.SCAN_FORMAT
    def outputFormat      = config.OUTPUT_FORMAT   // currently unused
    def severity          = config.SEVERITY ?: "HIGH,MEDIUM,LOW"
    def projectName       = config.PROJECT_NAME
    def component         = config.COMPONENT
    def gitLatestCommitId = config.MY_GIT_LATEST_COMMIT_ID

    //----------------------------------------------------
    // Validate mode
    //----------------------------------------------------
    if (!(mode in ['fs', 'image'])) {
        error "❌ Invalid MODE '${config.MODE}'. Choose 'fs' or 'image'."
    }

    //----------------------------------------------------
    // Derive file extension
    //----------------------------------------------------
    def ext = [
        "table": "txt",
        "json" : "json",
        "sarif": "sarif",
        "xml"  : "xml",
        "yaml" : "yaml"
    ][scanFormat] ?: scanFormat

    //----------------------------------------------------
    // Output file path
    //----------------------------------------------------
    def outputDir    = "trivy-reports"
    sh "mkdir -p ${outputDir}"

    def outputReport = "${outputDir}/${projectName}-${component}-${mode}-${gitLatestCommitId}.${ext}"

    echo "🛡 Running Trivy scan"
    echo "📄 Output file : '${outputReport}'"
    echo "🎯 Target      : '${target}'"

    //----------------------------------------------------
    // Call the class in src/com/sharedlib/TrivyScan.groovy
    //----------------------------------------------------
    def trivyScanParams = [
        MODE: mode,
        TARGET: target,
        SCAN_FORMAT: scanFormat,
        OUTPUT_REPORT: outputReport,
        SEVERITY: severity
    ]

    return new TrivyScan(this).trivyScan(trivyScanParams)
}
