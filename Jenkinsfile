pipeline {
    agent {label "java_agent"}

    environment{
        SONAR_HOST_URL=credentials('sonar_host')
        SONAR_TOKEN=credentials('SONAR_TOKEN')
        ECR_URI="352731040690.dkr.ecr.ap-south-1.amazonaws.com"
        AWS_CREDS = credentials('aws-creds')
        AWS_DEFAULT_REGION='ap-south-1'
        // ENV_FILE=credentials('ENV_FILE_JAY')
    }

    stages {

        stage('AWS Login') {
            
            steps {
            
                sh '''
                aws configure set aws_access_key_id  $AWS_ACCESS_KEY_ID
                aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                aws configure set region $AWS_DEFAULT_REGION
                aws sts get-caller-identity

                '''
            
        }
        }

        stage('Sonarqube static code analysis') {
    steps {
        script {
            
            def scannerHome = tool 'SonarQube'
            
            withSonarQubeEnv('Sonar') {
                sh """
                    ${scannerHome}/bin/sonar-scanner \
                    -Dsonar.projectKey=your-project-key \
                    -Dsonar.sources=. \
                    -Dsonar.host.url=${SONAR_HOST_URL} \
                    -Dsonar.login=${SONAR_TOKEN}
                """
            }
        }
    }
}


        stage('Build docker image and push to ECR') {
            steps {
                sh '''
                    aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ECR_URI}
                    sudo docker build -f Dockerfile_java -t javabackend:${env.BUILD_NUMBER}
                    sudo docker tag javabakend:${env.BUILD_NUMBER} ${ECR_URI}/javabcakend:${env.BUILD_NUMBER}
                    sudo docker push ${ECR_URI}/javabackend:${env.BUILD_NUMBER}

                '''
            }

        }

        stage('Docker container run'){
            steps {
                sh '''
                ssh -i key133.pem ubuntu@${SERVER_IP} \"
                    sudo docker pull 352731040690.dkr.ecr.ap-south-1.amazonaws.com/javabackend:${env.BUILD_NUMBER}
                    sudo docker stop javacont || true
                    sudo docker rm javacont || true
                    sudo docker run -d --name javacont -p 8001:8080 --env-file ${ENV_FILE} ${ECR_URI}/javabackend:${env.BUILD_NUMBER}

                    \"
                '''

            }

        }

        

    }

    

}