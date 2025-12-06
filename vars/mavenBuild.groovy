#!/usr/bin/env groovy
import com.sharedlib.MavenBuild

def call(Map config = [:]) {
    return new MavenBuild(this).mavenBuild(config)
}
