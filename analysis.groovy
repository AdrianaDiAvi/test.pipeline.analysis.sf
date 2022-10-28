def validation_kpi(additionalArgs){
  checkout([$class: 'GitSCM', doGenerateSubmoduleConfigurations: false,
    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${Workspace}"]],
    submoduleCfg: [], branches: [[name: 'development']], userRemoteConfigs: [[credentialsId: 'one-source-token-personal',
    url: 'https://github.com/AdrianaDiAvi/test.pipeline.analysis.sf.git']]])
 dir("${WORKSPACE}"){
  sh '''
    python3 send-data-wiki.py ${additionalArgs}
    '''
    
  }
}

return this