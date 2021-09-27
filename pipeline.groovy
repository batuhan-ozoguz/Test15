pipeline {
    agent any
    
    stages {
        stage("pre-build"){
            steps {
                sh script: '''
                pwd 
                ''' 
            }
        }
        
        stage("build"){
            steps {
                sh script: '''
                pwd
                rm -rf /home/jenkins/*
                git clone --branch $source_branch https://github.com/batuhan-ozoguz/Test15.git /home/jenkins/repo
                cd /home/jenkins/repo
                ''' 
            }
        }
        
        stage("pre test"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                yarn
                ''' 
            }
        }

        stage("yarn lint"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                yarn lint
                ''' 
            }
        }

        stage("yarn format"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                yarn format:check
                ''' 
            }
        }
        stage("yarn test"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                yarn test
                ''' 
            }
        }
        stage("yarn cov"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                yarn test:cov
                ''' 
            }    
        }
        stage("yarn e2e"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                yarn 
                ''' 
            }
        }

        stage("push code to repository"){
            steps {
                sh script: '''
                pwd
                cd /home/jenkins/repo
                git checkout $updated_branch
                git commit -m "$commit_message"
                git push https://github.com/batuhan-ozoguz/Test15.git $updated_branch 
                ''' 
            }
        }    
    }
}
