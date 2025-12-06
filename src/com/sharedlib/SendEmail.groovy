#!/usr/bin/env groovy
package com.sharedlib

class SendEmail implements Serializable {
    def script

    SendEmail(script) { this.script = script }

    def sendEmail(Map config = [:]) {

        def required = [
          "JOB_NAME",
          "BUILD_NUMBER",
          "BUILD_URL",
          "BRANCH_NAME",
          "PIPELINE_STATUS",
          "DURATION",
          "FROM_MAIL", 
          "TO_MAIL", 
          "REPLY_TO_MAIL",
          "CC_MAIL",
          "BCC_MAIL",
          "ATTACHMENTS"
        ]
        required.each { key ->
            if (!config[key] || config[key].toString().trim() == "") {
                script.error "❌ SEND EMAIL: Missing required parameter '${key}'"
            }
        }

        def jobName        = config.JOB_NAME
        def buildNumber    = config.BUILD_NUMBER
        def buildURL       = config.BUILD_URL
        def branchName     = script.env.BRANCH_NAME ?: 
        def pipelineStatus = script.currentBuild.currentResult ?: "UNKNOWN"
        def duration       = script.currentBuild.durationString
        def fromMail       = config.FROM_MAIL
        def toMail         = config.TO_MAIL
        def replyToMail    = config.REPLY_TO_MAIL
        def ccMail         = config.CC_MAIL ?: null
        def bccMail        = config.BCC_MAIL ?: null
        def attachments    = config.ATTACHMENTS ?: null

        def bannerColorMap = [
            "SUCCESS" : "#28a745",   // Green
            "UNSTABLE": "#ffc107",   // Yellow
            "FAILURE" : "#dc3545",   // Red
            "ABORTED" : "#6c757d",   // Gray
            "NOT_BUILT": "#6c757d"
        ]

        def bannerColor = config.bannerColor ?: bannerColorMap[pipelineStatus.toUpperCase()?.trim()] ?: "#007bff" // default blue

        def body = """
         <html>
            <body style="font-family: Arial, sans-serif;">
              <div style="border: 4px solid ${bannerColor}; padding: 15px; border-radius: 6px;">

                <h2 style="margin-bottom: 10px;">${jobName} - Build #${buildNumber}</h2>

                <div style="background-color: ${bannerColor}; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
                  <h3 style="color: white; margin: 0;">Pipeline Status: ${pipelineStatus.toUpperCase()}</h3>
                </div>

                <table style="border-collapse: collapse; width: 100%; margin-bottom: 15px;">
                  <tr>
                    <th style="border: 1px solid #ddd; padding: 8px;">Parameter</th>
                    <th style="border: 1px solid #ddd; padding: 8px;">Value</th>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 8px;">Job Name</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">${jobName}</td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 8px;">Build Number</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">${buildNumber}</td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 8px;">Branch</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">${branchName}</td>
                  </tr>
                  <tr>
                    <td style="border: 1px solid #ddd; padding: 8px;">Duration</td>
                    <td style="border: 1px solid #ddd; padding: 8px;">${duration}</td>
                  </tr>
                </table>

                <p>
                  Check full details in the
                  <a href="${buildURL}" style="color: #1a73e8;">Jenkins Console Output</a>.
                </p>
                </div>
            </body>
         </html>
        """

        // Send Email

        script.emailext(
            subject: "${jobName} - Build #${buildNumber} - ${pipelineStatus.toUpperCase()}",
            body: body,
            mimeType: "text/html",
            to: toMail,
            from: fromMail,
            replyTo: replyToMail,
            cc: ccMail,
            bcc: bccMail,
            attachmentsPattern: attachments
        )
        
        script.echo "✔ Email successfully sent"
    }
}
