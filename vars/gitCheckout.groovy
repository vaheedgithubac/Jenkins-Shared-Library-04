#!/usr/bin/env groovy
import com.sharedlib.GitCheckout

def call(Map config = [:]) {

    return new GitCheckout(this).gitCheckout(config)

   // The above step will return the latestCommitId from src/sharedlib/GitCheckout class
}
