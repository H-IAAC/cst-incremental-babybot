#!/bin/bash



# Cria a pasta de build se ainda não existir
mkdir -p build lib

# Instala o JDK necessário
apt-get update && apt-get install -y openjdk-11-jdk wget git

# Clona o repositório do CST e compila
git clone https://github.com/CST-Group/cst.git /opt/cst
cd /opt/cst || exit 1
./gradlew build

# Copia os arquivos JAR do CST para o projeto
cp /opt/cst/build/libs/*.jar /sharevnc/1_MIMoCoreModelExample/lib/

# Clona o repositório do CST Desktop e compila
git clone https://github.com/CST-Group/cst-desktop.git /opt/cst-desktop
cd /opt/cst-desktop || exit 1
./gradlew build

# Copia os arquivos JAR do CST Desktop para o projeto
cp /opt/cst-desktop/build/libs/*.jar /sharevnc/1_MIMoCoreModelExample/lib/

#!/bin/bash

# Caminho original do MindViewer
TARGET="/opt/cst-desktop/src/main/java/br/unicamp/cst/minds/core/MindViewer.java"

# Caminho para o novo arquivo limpo
CUSTOM="custom/MindViewer_NoSoar.java"

# Substituição
cp "$CUSTOM" "$TARGET"
echo "MindViewer.java substituído com versão sem SOAR."




