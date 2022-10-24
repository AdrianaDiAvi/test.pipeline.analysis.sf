#!/usr/bin/env groovy

pipeline {
    agent {
        label 'service-fw'
    }
    options {
        timestamps()
    }
        environment {
        GITHUB_CREDS = credentials('one-source-token-personal')
        ONESOURCE_DIR = "pre-silicon-triage"
        ONESOURCE_REPO =   "github.com/AdrianaDiAvi/applications.infrastructure.services-framework.pre-silicon-triage.git"
        ARTIFACTORY_CREDS = credentials('artifactory-token')
        ARTIFACTORY = credentials('artifactory-cred')
        ARTIFACTORY_REPO = "https://ubit-artifactory-or.intel.com/artifactory/presipipeline-or-local"
        ONESOURCE_DIR_WIKI = "${WORKSPACE}/wiki"
        ONESOURCE_WIKI_REPO = "github.com/AdrianaDiAvi/applications.benchmarking.benchmark.platform-hero-features.wiki.git"
        WIKI_MAIN_BRANCH='master'
    }

    parameters {
        choice(name: 'VERSION',
         choices: ['21.39', '21.47', '21.52', '22.05', '22.13', '22.18', '22.27', '22.33', '22.44', '22.50','22.54', '22.60', '22.66'],
         description:'Services Framework Version')
        choice(name: 'TOOL',
         choices: ['Validation Report Wiki', 'KPI Report Wiki', 'Validation Report Cumulus'],
         description:'Services Framework Tools')
    }
    stages {
        stage('Setup Repo Triage'){
            steps {
            
            sh '''

            mkdir ${ONESOURCE_DIR}
            git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_REPO} ${ONESOURCE_DIR}
            
            '''
        
        dir("${WORKSPACE}/${ONESOURCE_DIR}"){

            sh '''
            python3 -m pip install -r requirements.txt
            curl -sSf -H "X-JFrog-Art-Api:${ARTIFACTORY_CREDS}" -O ${ARTIFACTORY_REPO}/docker-triage.tar
            docker load -i docker-triage.tar
            docker images
            docker ps
            docker image tag 6ed8a78f6466  triage-builder:latest
            docker images

            '''
            }
            input('Do you want to proceed')
            }
        
        }

        stage('Setup Repo Wiki'){
            steps {
            
            sh '''

            mkdir ${ONESOURCE_DIR_WIKI}
            git -C ${ONESOURCE_DIR_WIKI} pull || git clone https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@${ONESOURCE_WIKI_REPO} ${ONESOURCE_DIR_WIKI}

            
            '''
        
        dir("${WORKSPACE}/${ONESOURCE_DIR_WIKI}"){

            sh '''
            git config user.email adriana.diaz.avila@intel.com
            git config user.name AdrianaDiAvi
            git checkout ${WIKI_MAIN_BRANCH}

            '''
            }
            input('Do you want to proceed')
            }
        }

        stage("Fuction to send data"){
            steps{
        dir("${WORKSPACE}/${ONESOURCE_DIR}"){
                script{
                switch(env.TOOL) {
                    case "Validation Report Wiki":
                        
                        sh '''
                        alias dotriage='docker run -i --rm -w `pwd` -v `pwd`:`pwd` -e no_proxy=".intel.com, 10.0.0.0/8" triage-builder'
                        dotriage ./build-database/generate-wiki-validation-report.py --collection "executions" > ${ONESOURCE_DIR_WIKI}/validation/pre_release_validation/release-pre-si-validation-v${VERSION}.md
                        pwd
                        '''
                        input('Do you want to proceed')
                        script{
                            def analysis = load "${WORKSPACE}/analysis.groovy"
                            analysis.validation()
                            }
                        input('Do you want to proceed')
                        dir("${ONESOURCE_DIR_WIKI}"){
                        sh '''
                        pwd
                        git status
                        sh script/update-homepage.sh > Home.md
                        git add .
                        git status
                        git commit -m "Testing Pipeline for Analysis SF"
                        git push origin master
                        git show
                        '''
                            }
                        input('Do you want to proceed')
                        break
                        
                    case "KPI Report Wiki":
                        sh '''
                        echo "the second case"
                        alias dotriage='docker run -i --rm -w `pwd` -v `pwd`:`pwd` -e no_proxy=".intel.com, 10.0.0.0/8" triage-builder'
                        dotriage ./build-database/generate-wiki-kpi-report.py --collection "executions" --test > ${ONESOURCE_DIR_WIKI}/validation/pre_release_kpi/release-pre-si-kpi-v${VERSION}.md --idsid "${ARTIFACTORY_USR}" --password "${ARTIFACTORY_PSW}"
                        '''
                        input('Do you want to proceed')
                        script{
                            def analysis = load "${WORKSPACE}/analysis.groovy"
                            analysis.KPI()
                            }
                        
                        input('Do you want to proceed')
                        dir("${ONESOURCE_DIR_WIKI}"){
                        sh '''
                        pwd
                        git status
                        sh script/update-homepage.sh > Home.md
                        git add .
                        git status
                        git commit -m "Testing Pipeline for Analysis SF"
                        git push origin master
                        git show
                        '''
                            }
                        input('Do you want to proceed')
                        
                        
                        break
                    case "Validation Report Cumulus":
                        sh '''
                        echo "the third case"
                        ./build-database/update-cumulus-validation-report.py  --release "v${VERSION}" --buildsdb "mongodb://presibuilds_ro:zCzRyEa9gJdAbU3@p1or1mon031.amr.corp.intel.com:7765,p2or1mon031.amr.corp.intel.com:7765,p3or1mon031.amr.corp.intel.com:7765/presibuilds?ssl=true&replicaSet=mongo7765" --cumulusdb "http://10.88.81.185:5000" --collection "executions_v2218"
                        '''
                        input('Do you want to proceed')
                        break
                    }
                }
            }
        
        }
    
        }
    }
    

  post {
    always {
        cleanWs()
        }
    } 

}