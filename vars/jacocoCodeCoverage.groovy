#!/usr/bin/env groovy
import com.sharedlib.JacocoCodeCoverage

def call(Map config = [:]) {

    return new JacocoCodeCoverage(this).jacocoCodeCoverage(config)

   // The above step will return the latestCommitId from src/sharedlib/GitCheckout class
}
