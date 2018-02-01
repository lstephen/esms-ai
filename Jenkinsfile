#!groovy

properties(
  [ [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', daysToKeepStr: '30'] ]
  , [$class: 'GithubProjectProperty', projectUrlStr: 'http://github.com/lstephen/esms-ai']
  , [$class: 'ParametersDefinitionProperty',
      parameterDefinitions:
      [ [$class: 'ChoiceParameterDefinition', name: 'ESMSAI_SITE', choices: "NONE\nEFL_TTH\nFFO_CSK\nSSL_MIS"]
      ]
    ]
  ])

def construi(target) {
  wrap([$class: 'AnsiColorBuildWrapper', colorMapName: 'xterm']) {
    sh "construi ${target}"
  }
}

def construi_on_node(target) {
  node('construi') {
    checkout scm
    construi target
  }
}

stage 'Build'
construi_on_node 'build'

if (env.BRANCH_NAME == 'master' && ESMSAI_SITE != 'NONE') {
  stage 'Run'
  node('construi') {
    checkout scm
    currentBuild.description = "Run ${ESMSAI_SITE}"

    withCredentials([
      [ $class: 'FileBinding'
        , variable: 'GIT_SSH_KEY'
        , credentialsId: 'cfbecb37-737f-4597-86f7-43fb2d3322cc' ]
      ]) {
      withEnv(
        [ "ESMSAI_SITE=${ESMSAI_SITE}"
        ]) {
        construi 'run'
      }
    }
  }
}

