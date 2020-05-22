FROM ubuntu:19.10

# https://askubuntu.com/a/1013396
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y install default-jdk curl && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src

RUN curl https://github.com/google/or-tools/releases/download/v7.6/or-tools_ubuntu-19.10_v7.6.7691.tar.gz -L | tar zx
RUN mv or-tools_Ubuntu-19.10-64bit_v7.6.7691/lib/ . && rm -rf or-tools_Ubuntu-19.10-64bit_v7.6.7691

COPY server/build/libs/server-0.0.1-all.jar /usr/src/server.jar

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-Djava.library.path=lib", "-jar", "server.jar"]
