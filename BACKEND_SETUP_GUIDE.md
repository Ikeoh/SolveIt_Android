# Guia de Configuração do Ambiente de Backend - SolveIt

Este documento detalha todos os passos necessários para configurar e executar o servidor de backend do projeto SolveIt em uma máquina Windows a partir do zero.

## Parte 1: Configuração do Banco de Dados (SQL Server)

O backend requer um banco de dados Microsoft SQL Server para funcionar.

### 1.1. Instalar o SQL Server via Docker

A maneira mais simples de rodar o SQL Server localmente é usando Docker.

1.  **Instale o Docker Desktop:** Baixe e instale o [Docker Desktop para Windows](https://www.docker.com/products/docker-desktop/).
2.  **Inicie o Container do SQL Server:** Abra um terminal (PowerShell ou Prompt de Comando) e execute o comando abaixo. **Importante:** Substitua `SuaSenhaForte#2024` por uma senha de sua escolha e guarde-a.

    ```bash
    docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=SuaSenhaForte#2024" -p 1433:1433 --name sql_server_demo -d mcr.microsoft.com/mssql/server:2019-latest
    ```

### 1.2. Criar a Base de Dados e as Tabelas

1.  **Instale uma Ferramenta de Gerenciamento de BD:** Baixe e instale uma ferramenta como o [Azure Data Studio](https://learn.microsoft.com/sql/azure-data-studio/download-azure-data-studio) ou [DBeaver](https://dbeaver.io/download/).
2.  **Conecte-se ao Banco de Dados:** Use as seguintes credenciais na sua ferramenta:
    *   **Servidor:** `localhost`
    *   **Login/Usuário:** `sa`
    *   **Senha:** A senha que você definiu no passo anterior (ex: `SuaSenhaForte#2024`).
3.  **Crie a Base de Dados:** Após conectar, execute o seguinte comando SQL:

    ```sql
    CREATE DATABASE SolveItDB;
    ```

4.  **Crie as Tabelas:** Certifique-se de que você está no contexto do banco `SolveItDB` e execute os scripts SQL abaixo, um de cada vez, na ordem correta.

    **Tabela `Empresa`:**
    ```sql
    CREATE TABLE Empresa (
        id_empresa INT PRIMARY KEY IDENTITY(1,1),
        nome_empresa VARCHAR(150) NOT NULL,
        cnpj VARCHAR(20) NOT NULL UNIQUE
    );
    ```

    **Tabela `TipoAcesso`:**
    ```sql
    CREATE TABLE TipoAcesso (
        id_tipo_acesso INT PRIMARY KEY IDENTITY(1,1),
        descricao VARCHAR(50) NOT NULL
    );
    ```

    **Tabela `Usuarios`:**
    ```sql
    CREATE TABLE Usuarios (
        id_usuario INT PRIMARY KEY IDENTITY(1,1),
        nome_usuario NVARCHAR(100) NOT NULL,
        email NVARCHAR(150) NOT NULL UNIQUE,
        senha_hash VARCHAR(255) NOT NULL,
        area NVARCHAR(100) NULL,
        cpf VARCHAR(14) NOT NULL UNIQUE,
        login_sugerido VARCHAR(100) NOT NULL UNIQUE,
        dt_cadastro DATETIME NOT NULL DEFAULT GETDATE(),
        st_usuario BIT NOT NULL DEFAULT 1,
        id_tipo_acesso INT NOT NULL,
        id_empresa INT NOT NULL,
        FOREIGN KEY (id_tipo_acesso) REFERENCES TipoAcesso(id_tipo_acesso),
        FOREIGN KEY (id_empresa) REFERENCES Empresa(id_empresa)
    );
    ```

    **Tabela `Chamados`:**
    ```sql
    CREATE TABLE Chamados (
        id_chamado INT PRIMARY KEY IDENTITY(1,1),
        titulo VARCHAR(100) NOT NULL,
        descricao TEXT NOT NULL,
        prioridade VARCHAR(20) NOT NULL,
        categoria VARCHAR(50) NOT NULL,
        status VARCHAR(30) NOT NULL DEFAULT 'Aberto',
        data_abertura DATETIME NOT NULL DEFAULT GETDATE(),
        id_usuario INT NOT NULL,
        FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
    );
    ```

5.  **Insira Dados Iniciais (Obrigatório):** A aplicação precisa de dados mínimos para iniciar.

    ```sql
    -- Insere um tipo de acesso "Padrão"
    INSERT INTO TipoAcesso (descricao) VALUES ('Padrao');

    -- Insere uma empresa de exemplo
    INSERT INTO Empresa (nome_empresa, cnpj) VALUES ('Empresa Exemplo', '00.000.000/0001-00');
    ```

## Parte 2: Configuração do Projeto

### 2.1. Configurar a Conexão com o Banco

1.  Navegue até a pasta `solveit-backend/src/main/resources/`.
2.  Crie uma cópia do arquivo `config.properties.example` e renomeie-a para `config.properties`.
3.  Abra o novo `config.properties` e adicione o seguinte conteúdo, substituindo a senha se necessário:

    ```properties
    db.url=jdbc:sqlserver://localhost:1433;databaseName=SolveItDB;trustServerCertificate=true
    db.user=sa
    db.password=SuaSenhaForte#2024
    ```

### 2.2. Configurar o JDK (Java Development Kit)

O Gradle precisa saber onde encontrar o JDK para compilar o projeto. A forma mais robusta é especificar o caminho diretamente no projeto.

1.  **Instale o JDK 21:** Se ainda não o tiver, instale o JDK 21 (da Oracle, Adoptium, etc.). O caminho de instalação padrão no Windows é `C:\Program Files\Java\jdk-21`.
2.  **Edite o arquivo `gradle.properties`** na raiz do projeto.
3.  Adicione a seguinte linha no final do arquivo, ajustando o caminho se a sua instalação for em outro local:

    ```properties
    org.gradle.java.home=C:/Program Files/Java/jdk-21
    ```
    *Nota: O uso de barras normais (`/`) é intencional e mais seguro para arquivos de configuração.*

### 2.3. Tornar o Backend Executável

Precisamos dizer ao Gradle que o módulo `solveit-backend` é um aplicativo executável.

1.  **Abra o arquivo `solveit-backend/build.gradle`**.
2.  Modifique o início do arquivo para que ele fique assim:

    ```groovy
    plugins {
        id 'java-library'
        id 'application' // Adiciona o plugin de aplicação
    }

    // Define a classe principal para o plugin 'application'
    application {
        mainClass = 'com.example.solveit_backend.ServerMain'
    }

    java {
        // ... (o resto do arquivo continua igual)
    ```

## Parte 3: Executando o Servidor

Após concluir todos os passos anteriores, você pode iniciar o servidor.

1.  Abra um terminal na raiz do projeto (`SolveIt_Android`).
2.  Execute o comando:

    ```bash
    .\gradlew.bat :solveit-backend:run
    ```

3.  Aguarde a compilação. Se tudo deu certo, você verá a mensagem de sucesso no terminal:

    ```
    API SolveIT (Servidor Jetty) INICIADA com sucesso!
    Acesse: http://localhost:8080/api/login
    ```

4.  Para **parar o servidor**, clique no terminal e pressione `Ctrl + C`.
