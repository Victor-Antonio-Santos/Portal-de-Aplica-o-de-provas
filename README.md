<img width="1876" height="928" alt="image" src="https://github.com/user-attachments/assets/f2716444-f036-4756-b1b4-6a66cedde956" />


# Provas Online

Projeto web corporativo em Java 8 com JSF, PrimeFaces, JPA/Hibernate e Maven para criacao, envio, realizacao e acompanhamento de provas online.

## Stack

- Java 8
- Maven
- JSF 2.2 com XHTML
- PrimeFaces 8
- JPA/Hibernate
- MySQL
- WildFly para execucao local

## Estrutura

- `src/main/java/br/com/empresa/provas/entity`
- `src/main/java/br/com/empresa/provas/dao`
- `src/main/java/br/com/empresa/provas/service`
- `src/main/java/br/com/empresa/provas/controller`
- `src/main/java/br/com/empresa/provas/util`
- `src/main/resources/META-INF/persistence.xml`
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/data.sql`
- `src/main/webapp/pages`

## Configuracao

1. Garantir um WildFly 14+ configurado no IntelliJ.
2. Criar o banco `provas_online` no MySQL.
3. Validar a conexao em `src/main/resources/META-INF/persistence.xml`.
4. Executar `mvn clean package` para gerar `target/provas-online.war`.
5. No IntelliJ, recarregar o projeto Maven.
6. Criar uma configuracao `Application Server > WildFly Server > Local`.
7. Em `Deployment`, adicionar `target/provas-online.war` ou recriar o artifact `war exploded` depois do reload do Maven.
8. Subir o servidor e acessar `http://localhost:8080/provas-online/pages/login.xhtml`.

## Ajuste para WildFly

- O contexto da aplicacao esta fixado em `/provas-online` por `src/main/webapp/WEB-INF/jboss-web.xml`.
- O projeto usa `RESOURCE_LOCAL` com `JPAUtil`, entao o Hibernate vai empacotado dentro do `war`.
- O arquivo `src/main/webapp/WEB-INF/jboss-deployment-structure.xml` desabilita o subsistema JPA do WildFly e exclui os modulos Hibernate do servidor.
- Esse ajuste evita o conflito `ClassCastException: org.dom4j.DocumentFactory cannot be cast to org.dom4j.DocumentFactory` causado pelo Envers do WildFly 14.
- O driver MySQL permanece empacotado no `war`, entao nao e necessario instalar modulo JDBC no servidor para este projeto-base.

## Scripts uteis

- `start-wildfly.cmd`: sobe o WildFly usando `E:\wildfly-14.0.0.Final` por padrao.
- `deploy-wildfly.cmd`: publica `target/provas-online.war` no WildFly via `jboss-cli`.

Se o IntelliJ estiver publicando um artifact antigo, apague e recrie o artifact antes do deploy.

## Acessos de teste

- Admin: CPF `529.982.247-25` / senha `admin123`
- Colaborador: CPF `111.444.777-35` / senha `colab123`
