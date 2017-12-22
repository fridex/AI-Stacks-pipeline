#!/usr/bin/groovy
package com.redhat.aicoe.pipeline

/**
 * create BuildConfigs from templates
 * @param openshiftProject OpenShift Project
 * @return
 */
def createBuildConfigs(String openshiftProject) { // TODO can we set a default value here?
    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            script {
                echo "TODO create all BuildConfigs from templates in pipeline-images/*/buildConfig.yaml"
            }
        }
    }
}

/**
 * Build container image in OpenShift and tag it
 * @param openshiftProject OpenShift Project
 * @param buildConfig
 * @param tag
 * @return
 */
def buildImageWithTag(String openshiftProject, String buildConfig, String tag) {
    // - build in OpenShift
    // - startBuild using ref in OpenShift
    // - Get result Build and get imagestream manifest
    // - Use that to create a tag
    openshift.withCluster() {
        openshift.withProject(openshiftProject) {
            def result = openshift.startBuild(buildConfig, "--wait")
            def buildName = result.out.trim()
            
            echo "Resulting Build: " + buildName

            def describeStr = openshift.selector(buildName).describe()
            buildName = describeStr.buildName.trim()

            def buildLog = openshift.getBuildPodLogs(buildConfig)

            echo "BuildLog: " + buildLog

            def imageHash = sh(
                    script: "echo \"${buildName}\" | grep 'Image Digest:' | cut -f2- -d:",
                    returnStdout: true
            ).trim()
            echo "imageHash: ${imageHash}"

            echo "Creating stable tag for ${openshiftProject}/${buildConfig}: ${buildConfig}:${tag}"

            openshift.tag("${openshiftProject}/${buildConfig}@${imageHash}",
                        "${openshiftProject}/${buildConfig}:${tag}")

        }
    }
}