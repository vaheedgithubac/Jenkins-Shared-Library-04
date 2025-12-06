#!/usr/bin/env groovy
import com.sharedlib.SendEmail

def call(Map config = [:]) {
    return new SendEmail(this).sendEmail(config)
}
