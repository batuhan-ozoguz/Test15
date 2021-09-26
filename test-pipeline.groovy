String kubernetesCredentialsId='mps-kube-config-dev-cluster'
String kubernetesNamespace='telco'
String kubernetesServerUrl='https://bce975d7-bef0-412b-a3ce-489691acf24d.k8s.ondigitalocean.com'

String gitCredentialsId = 'mps-gitlab-rw-deploy-key'
String gitUrl = 'git@git.telco.inomera.com:tt/digital-transformation/cms/cms-catalog.git'

String kubernetesContainerName = 'ddp-cms-catalog-container'
String kubernetesPodName = 'ddp-cms-catalog-pod'
String dockerImageUrl = 'docker.inomera.com/tt-dev/tt-ddp/cms-catalog'

VERSION = 'null'
EXTRA_BUILD_ARGS = ""

if (params.JENKINS_FORCE_NEW_VERSION) {
    EXTRA_BUILD_ARGS = "--force-new-version"
}

node ('') {
    stage('Clean Workspace') {
        cleanWs()
    }

    stage('Check Kubectl') {
        withKubeConfig([credentialsId: kubernetesCredentialsId, namespace: kubernetesNamespace, serverUrl: kubernetesServerUrl]) {
            sh "kubectl get pods -n ${kubernetesNamespace}"
        }
    }

    stage('Poll SCM') {
        println 'Debug: Poll SCM Started'

        git branch: 'master',
                credentialsId: gitCredentialsId,
                url: gitUrl

        println 'Debug: Poll SCM Finished'
    }

    stage('Build') {
        println "Debug: Build started"

        sh "git branch --set-upstream-to=origin/master master"
        sh "git config user.email \"simpl@inomera.com\""
        sh "git config user.name \"SIMPL on Jenkins\""
        
        jdk = tool name: 'oraclejdk-11.0.11'
        println "Using JDK: ${jdk}"
        
        env.JAVA_HOME = "${jdk}"
        env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
        env.EXT_GIT_BRANCH="${params.GIT_BRANCH}"
        sh 'java -version'

        sshagent(credentials : [gitCredentialsId]) {
            sh "bash -ex release.sh ${params.JENKINS_RELEASE_TYPE} ${EXTRA_BUILD_ARGS}"
        }

        println "Debug: Build finished"
    }

    stage('Get Version') {
        println 'Debug: Get Version Started'

        VERSION = readFile('VERSION').trim()

        println "Debug: Get Version Finished : ${VERSION}"
    }

    stage('Replace pod image') {
        println 'Debug: Replace pod image started'

        def image = "${kubernetesContainerName}=${dockerImageUrl}:${VERSION}"
        println "Debug: Image to update : ${image}"

        withKubeConfig([credentialsId: kubernetesCredentialsId, namespace: kubernetesNamespace, serverUrl: kubernetesServerUrl]) {
            sh "kubectl set image deployment/${kubernetesPodName} ${image} -n ${kubernetesNamespace}"
        }
    }
}