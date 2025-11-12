pipeline {
    agent {label "java_agent"}

    environment{
        SONAR_HOST_URL=credentials('sonar_host')
        SONAR_TOKEN=credentials('SONAR_TOKEN')
        ECR_URI=credentials('ECR_URI')
        AWS_CREDS = credentials('aws-creds')
        AWS_DEFAULT_REGION='ap-south-1'
        SPRING_DATASOURCE_URL=credentials('SPRING_DATASOURCE_URL')
        SPRING_DATASOURCE_USERNAME=credentials('DB_USER')
        SPRING_DATASOURCE_PASSWORD=credentials('DB_PASSWORD')
        SECURITY_JWT_SECRET_KEY=credentials('SECURITY_JWT_SECRET_KEY')
        JWT_REFRESH_EXPIRATION_MS=credentials('JWT_REFRESH_EXPIRATION_MS')
        CLOUDINARY_CLOUD_NAME=credentials('CLOUDINARY_CLOUD_NAME')
        CLOUDINARY_API_KEY=credentials('CLOUDINARY_API_KEY')
        CLOUDINARY_API_SECRET=credentials('CLOUDINARY_API_SECRET')
        SERVER_IP=credentials('SERVER_IP')
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

        stage('Build') {
            steps {
                sh '''
                export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
                export PATH=$JAVA_HOME/bin:$PATH
                mvn clean install -DskipTests
                '''
            }
        }
        
        stage('SonarQube static code analysis') {
        steps {
            withSonarQubeEnv('Sonar') { 
               sh '''
                mvn sonar:sonar \
                    -Dsonar.projectKey=inventory-management-system \
                    -Dsonar.host.url=$SONAR_HOST_URL \
                    -Dsonar.login=$SONAR_AUTH_TOKEN
            '''
            }
        }
    }



        stage('Build docker image and push to ECR') {
            steps {
                sh """
                    export PATH=$PATH:/usr/local/bin
                    aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ECR_URI}
                    sudo docker build -t javabackend:${env.BUILD_NUMBER} .
                    sudo docker tag javabackend:${env.BUILD_NUMBER} ${ECR_URI}/javabackend:${env.BUILD_NUMBER}
                    sudo docker push ${ECR_URI}/javabackend:${env.BUILD_NUMBER}

                """
            }

        }

        stage('Docker container run'){
            steps {
                sh """
                ssh -o StrictHostKeyChecking=no -i /home/ubuntu/new-key ubuntu@${SERVER_IP} \"
                    aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ECR_URI}
                    sudo docker pull 352731040690.dkr.ecr.ap-south-1.amazonaws.com/javabackend:${env.BUILD_NUMBER}
                    sudo docker stop javacont || true
                    sudo docker rm javacont || true
                    sudo docker run -d --name javacont -p 8001:8080 -e SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL} -e SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME} -e SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD} -e SECURITY_JWT_SECRET_KEY=${SECURITY_JWT_SECRET_KEY} -e JWT_REFRESH_EXPIRATION_MS=${JWT_REFRESH_EXPIRATION_MS} -e CLOUDINARY_CLOUD_NAME=${CLOUDINARY_CLOUD_NAME} -e CLOUDINARY_API_KEY=${CLOUDINARY_API_KEY} -e CLOUDINARY_API_SECRET=${CLOUDINARY_API_SECRET} ${ECR_URI}/javabackend:${env.BUILD_NUMBER}

                    \"
                """

            }

        }

        

    }

    

}